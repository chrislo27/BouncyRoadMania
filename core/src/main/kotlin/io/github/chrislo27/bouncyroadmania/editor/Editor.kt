package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ActionHistory
import io.github.chrislo27.bouncyroadmania.editor.rendering.EditorRenderer
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.properties.Delegates


class Editor(val main: BRManiaApp) : ActionHistory<Editor>(), InputProcessor {

    companion object {
        const val EVENT_HEIGHT: Float = 48f
        const val EVENT_WIDTH: Float = EVENT_HEIGHT * 4
        const val TRACK_LINE_THICKNESS: Float = 2f
        const val SELECTION_BORDER: Float = 2f
        internal const val NEGATIVE_SYMBOL = "-"
        internal const val ZERO_BEAT_SYMBOL = "♩"
        internal const val SELECTION_RECT_ADD = "+"
        internal const val SELECTION_RECT_INVERT = "±"
        internal val THREE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.000", DecimalFormatSymbols())
        internal val TRACKER_TIME_FORMATTER = DecimalFormat("00.000", DecimalFormatSymbols())
        internal val TRACKER_MINUTES_FORMATTER = DecimalFormat("00", DecimalFormatSymbols())
        val ONE_DECIMAL_PLACE_FORMATTER = DecimalFormat("0.0", DecimalFormatSymbols())
        val TWO_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.00", DecimalFormatSymbols())
    }

    val theme: EditorTheme get() = main.editorTheme

    var editMode: EditMode by Delegates.observable(EditMode.EVENTS) { _, oldVal, newVal ->
        stage.decideVisibility()
        updateMessageBar()
    }
    val stage: EditorStage = EditorStage(this)
    val engine: Engine = Engine()
    var currentTool: Tool by Delegates.observable(Tool.SELECTION) { _, _, _ -> updateMessageBar() }
    var snap: Float = 0.25f
    var clickOccupation: ClickOccupation = ClickOccupation.None
    var selection: List<Event> = listOf()

    val renderer: EditorRenderer = EditorRenderer(this)
    private var frameLastCallUpdateMessageBar: Long = -1
    var cachedPlaybackStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""
    var cachedMusicStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""

    private val mouseVector: Vector2 = Vector2()
        get() {
            field.set(renderer.trackCamera.getInputX(), renderer.trackCamera.getInputY())
            return field
        }

    init {
        Localization.addListener {
            updateMessageBar()
        }
    }

    fun updateMessageBar() {
        frameLastCallUpdateMessageBar = Gdx.graphics.frameId
        val message = StringBuilder()
        val controls = StringBuilder()

        when (editMode) {
            EditMode.PARAMETERS -> message.append(Localization["editor.msg.parameters"])
            else -> {
            }
        }

        stage.messageLabel.text = message.toString()
        stage.controlsLabel.text = controls.toString()
    }

    private fun setSubbeatSectionToMouse() {
        val subbeatSection = renderer.subbeatSection
        subbeatSection.enabled = true
        subbeatSection.start = Math.floor(renderer.trackCamera.getInputX().toDouble()).toFloat()
        subbeatSection.end = subbeatSection.start
    }

    fun renderUpdate() {
        engine.update(Gdx.graphics.deltaTime)
        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()

        if (editMode == EditMode.EVENTS) {
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                renderer.trackCamera.position.x -= renderer.toScaleX(Editor.EVENT_WIDTH * 5 * Gdx.graphics.deltaTime * if (shift xor control) 6f else 1f)
                renderer.cameraPan = null
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                renderer.trackCamera.position.x += renderer.toScaleX(Editor.EVENT_WIDTH * 5 * Gdx.graphics.deltaTime * if (shift xor control) 6f else 1f)
                renderer.cameraPan = null
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                renderer.cameraPan = CameraPan(renderer.trackCamera.position.x, 0f, 0.25f, Interpolation.exp10Out)
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                renderer.cameraPan = CameraPan(renderer.trackCamera.position.x, engine.lastPoint, 0.25f, Interpolation.exp10Out)
            }
        }

        renderer.subbeatSection.enabled = false

        if (!stage.isTyping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                Gdx.input.inputProcessor.scrolled(-1)
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || (Gdx.input.isKeyJustPressed(Input.Keys.S) && !control)) {
                Gdx.input.inputProcessor.scrolled(1)
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (shift) {
                    when (engine.playState) {
                        PlayState.STOPPED, PlayState.PAUSED -> engine.playState = PlayState.PLAYING
                        PlayState.PLAYING -> engine.playState = PlayState.PAUSED
                    }
                } else {
                    when (engine.playState) {
                        PlayState.STOPPED, PlayState.PAUSED -> engine.playState = PlayState.PLAYING
                        PlayState.PLAYING -> engine.playState = PlayState.STOPPED
                    }
                }
            }

            run clickCheck@{
                val clickOccupation = clickOccupation
                val tool = currentTool
                val camera = renderer.trackCamera
                val nearestSnap = MathHelper.snapToNearest(camera.getInputX(), snap)
                if (tool == Tool.SELECTION) {
                    when (clickOccupation) {
                        is ClickOccupation.Music -> {
                            setSubbeatSectionToMouse()
                            engine.musicStartSec = if (Gdx.input.isShiftDown()) camera.getInputX() else nearestSnap
                        }
                        is ClickOccupation.Playback -> {
                            setSubbeatSectionToMouse()
                            engine.playbackStart = nearestSnap
                        }
                        is ClickOccupation.SelectionDrag -> {
                            if (clickOccupation.isStretching) {
                                val rootEntity = clickOccupation.clickedOn
                                val rootBound = clickOccupation.oldBounds.getValue(rootEntity)

                                fun stretch(entity: Event) {
                                    val oldBound = clickOccupation.oldBounds.getValue(entity)
                                    entity.updateBounds {
                                        if (clickOccupation.stretchType == StretchRegion.LEFT) {
                                            val oldRightSide = oldBound.x + oldBound.width

                                            entity.bounds.x = (nearestSnap - (rootBound.x - oldBound.x)).coerceAtMost(oldRightSide - Event.MIN_STRETCH)
                                            entity.bounds.width = oldRightSide - entity.bounds.x
                                        } else if (clickOccupation.stretchType == StretchRegion.RIGHT) {
                                            entity.bounds.width = (nearestSnap - oldBound.x - (rootBound.maxX - oldBound.maxX)).coerceAtLeast(Event.MIN_STRETCH)
                                        }
                                    }
                                }

                                stretch(rootEntity)
                                this.selection.forEach { entity ->
                                    if (entity === rootEntity) return@forEach
                                    stretch(entity)
                                }
                            } else {
                                clickOccupation.setPositionRelativeToMouse()
                            }

                            val subbeatSection = renderer.subbeatSection
                            subbeatSection.enabled = true
                            subbeatSection.start = Math.floor(clickOccupation.left.toDouble()).toFloat()
                            subbeatSection.end = clickOccupation.right

                            updateMessageBar()
                        }
                        is ClickOccupation.CreatingSelection -> {
                            clickOccupation.updateRectangle()
                            updateMessageBar()
                        }
                        ClickOccupation.None -> {
                            if (selection.isNotEmpty() && !stage.isTyping) {
                                if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                                    this.selection.forEach { engine.removeEvent(it) }
//                                    remix.addActionWithoutMutating(ActionGroup(listOf(
//                                            EntityRemoveAction(this, this.selection,
//                                                    this.selection.map { Rectangle(it.bounds) }),
//                                            EntitySelectionAction(this, this.selection.toList(), listOf())
//                                    )))
                                    this.selection = listOf()

                                    updateMessageBar()
                                }
                            }
                        }
                        is ClickOccupation.TrackerResize -> {
                            // handled below
                        }
                    }
                } else if (tool.isTrackerRelated) {
                    if (clickOccupation is ClickOccupation.TrackerResize) {
                        clickOccupation.updatePosition(nearestSnap)
                    }
                }
            }
        }

        if (engine.playState == PlayState.PLAYING) {
            renderer.trackCamera.position.x = engine.tempos.linearSecondsToBeats(engine.seconds) + renderer.trackCamera.viewportWidth * 0.25f
        }

        if (currentTool.isTrackerRelated) {
            updateMessageBar()
        }

        if (engine.playState != PlayState.STOPPED)
            return

//        run stretchCursor@{
//            val clickOccupation = clickOccupation
//            val shouldStretch = engine.playState == PlayState.STOPPED && currentTool == Tool.SELECTION &&
//                    ((clickOccupation is ClickOccupation.SelectionDrag && clickOccupation.isStretching) ||
//                            (clickOccupation == ClickOccupation.None && this.selection.isNotEmpty() && this.selection.all { it is IStretchable && it.isStretchable } && remix.entities.any {
//                                canStretchEntity(mouseVector, it)
//                            }))
//
//            if (wasStretchCursor && !shouldStretch) {
//                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
//                wasStretchCursor = shouldStretch
//            } else if (!wasStretchCursor && shouldStretch) {
//                Gdx.graphics.setCursor(AssetRegistry["cursor_horizontal_resize"])
//                wasStretchCursor = shouldStretch
//            }
//        }


        if (currentTool.showSubbeatLines) {
            val subbeatSection = renderer.subbeatSection
            subbeatSection.enabled = true
            subbeatSection.start = Math.floor(renderer.trackCamera.getInputX().toDouble()).toFloat()
            subbeatSection.end = subbeatSection.start + 0.5f
        }

    }

    fun getTrackerOnMouse(klass: Class<out Tracker<*>>?, obeyY: Boolean): Tracker<*>? {
        if (klass == null || (obeyY && renderer.trackCamera.getInputY() > 0f))
            return null
        val mouseX = renderer.trackCamera.getInputX()
        engine.trackers.forEach { container ->
            val result = container.map.values.firstOrNull {
                if (it::class.java != klass)
                    false
                else if (it.isZeroWidth)
                    MathUtils.isEqual(mouseX, it.beat, snap * 0.5f)
                else
                    mouseX in it.beat..it.endBeat
            }

            if (result != null)
                return result
        }

        return null
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (clickOccupation != ClickOccupation.None || engine.playState != PlayState.STOPPED || editMode != EditMode.EVENTS)
            return false

        val mouseVector = mouseVector
        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()
        val alt = Gdx.input.isAltDown()

        val isMusicTrackerButtonDown = !(shift || alt) &&
                ((!control && button == Input.Buttons.MIDDLE) || (button == Input.Buttons.RIGHT && control))

        val isPlaybackTrackerButtonDown = !isMusicTrackerButtonDown &&
                button == Input.Buttons.RIGHT && !(shift || alt || control)

        val isAnyTrackerButtonDown = isMusicTrackerButtonDown || isPlaybackTrackerButtonDown

        val isDraggingButtonDown = button == Input.Buttons.LEFT
        val isCopying = isDraggingButtonDown && alt

        val tool = currentTool
        if (tool == Tool.SELECTION) {
            val firstEventInMouse: Event? = engine.events.firstOrNull { mouseVector in it.bounds }
            if (isAnyTrackerButtonDown && firstEventInMouse == null) {
                clickOccupation = if (isMusicTrackerButtonDown) {
                    ClickOccupation.Music(this, button == Input.Buttons.MIDDLE)
                } else {
                    ClickOccupation.Playback(this)
                }
            }
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val clickOccupation = clickOccupation
        if (clickOccupation is ClickOccupation.Music &&
                (button == (if (clickOccupation.middleClick) Input.Buttons.MIDDLE else Input.Buttons.RIGHT))) {
            if (engine.musicStartSec != clickOccupation.old) {
                clickOccupation.final = engine.musicStartSec
                this.addActionWithoutMutating(clickOccupation)
            }
            this.clickOccupation = ClickOccupation.None

            return true
        } else if (clickOccupation is ClickOccupation.Playback &&
                button == Input.Buttons.RIGHT) {
            if (engine.playbackStart != clickOccupation.old) {
                clickOccupation.final = engine.playbackStart
                this.addActionWithoutMutating(clickOccupation)
            }
            this.clickOccupation = ClickOccupation.None
            return true
        } else if (clickOccupation is ClickOccupation.SelectionDrag) {
            val validPlacement = clickOccupation.isPlacementValid()
            val deleting = clickOccupation.isInDeleteZone()

            /*
             Placement = drag from picker or copy
             Movement  = moving existing entities

             Outcomes:

             New or copy:
             Correct placement -> results in a place+selection action
             Invalid placement -> remove, restore old selection

             Existing:
             Correct movement  -> results in a move action
             Invalid movement  -> return
             Delete movement   -> remove+selection action
             */

            if (clickOccupation.isNewOrCopy) {
                if (validPlacement) {
                    // place+selection action
//                    remix.addActionWithoutMutating(ActionGroup(listOf(
//                            EntityPlaceAction(this, this.selection),
//                            EntitySelectionAction(this, clickOccupation.previousSelection, this.selection)
//                    )))
                } else {
                    // delete silently
                    selection.forEach { engine.removeEvent(it) }
                    // restore original selection
                    selection = clickOccupation.previousSelection
                }
            } else {
//                if (validPlacement) {
//                    // move action
//                    val sel = this.selection.toList()
//                    remix.addActionWithoutMutating(EntityMoveAction(this, sel, sel.map { clickOccupation.oldBounds.getValue(it) }))
//                } else if (deleting) {
//                    // remove+selection action
//                    remix.entities.removeAll(this.selection)
//                    selection.filterIsInstance<ModelEntity<*>>().forEach { explodeEntity(it) }
//                    val sel = this.selection.toList()
//                    remix.addActionWithoutMutating(ActionGroup(listOf(
//                            EntityRemoveAction(this, this.selection, sel.map { clickOccupation.oldBounds.getValue(it) }),
//                            EntitySelectionAction(this, clickOccupation.previousSelection, listOf())
//                    )))
//                    this.selection = listOf()
//                } else {
//                    // revert positions silently
//                    clickOccupation.oldBounds.forEach { entity, rect ->
//                        entity.updateBounds {
//                            entity.bounds.set(rect)
//                        }
//                    }
//                }
            }

            this.clickOccupation = ClickOccupation.None
            engine.recomputeCachedData()
            return true
        } else if (clickOccupation is ClickOccupation.CreatingSelection &&
                (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT)) {
            /*
            Selections are now actions and can be undone
            Note that a selection change will also have to occur when you drag new things - this is handled
             */

            if (button == Input.Buttons.LEFT) {
                // finish selection as ACTION
                clickOccupation.updateRectangle()
                val selectionRect = clickOccupation.rectangle
//                val newSelection: List<Entity> = getSelectionMode().createNewSelection(remix.entities.toList(), this.selection.toList(), selectionRect)
//                if (!this.selection.containsAll(newSelection) ||
//                        (newSelection.size != this.selection.size)) {
//                    remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
//                }
            }

            this.clickOccupation = ClickOccupation.None
        } else if (clickOccupation is ClickOccupation.TrackerResize) {
            clickOccupation.normalizeWidth()
            if (button == Input.Buttons.LEFT && clickOccupation.isPlacementValid() &&
                    (clickOccupation.tracker.beat != clickOccupation.beat || clickOccupation.tracker.width != clickOccupation.width)) {
                val copy = clickOccupation.tracker.createResizeCopy(clickOccupation.beat, clickOccupation.width)
//                remix.mutate(ActionGroup(
//                        TrackerAction(clickOccupation.tracker, true),
//                        TrackerAction(copy, false)
//                ))
            }

            this.clickOccupation = ClickOccupation.None
        }

        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }


    fun getDebugString(): String {
        return "updateMessageBar: ${Gdx.graphics.frameId - frameLastCallUpdateMessageBar}" + "\n${renderer.getDebugString()}"
    }
}