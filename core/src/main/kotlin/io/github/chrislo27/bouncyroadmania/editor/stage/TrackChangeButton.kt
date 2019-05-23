package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.bouncyroadmania.editor.CameraPan
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.impl.EventSelectionAction
import io.github.chrislo27.bouncyroadmania.editor.oopsies.impl.TrackResizeAction
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.event.EndEvent
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import kotlin.math.roundToInt

class TrackChangeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    private val engine: Engine
        get() = editor.engine

    override var tooltipText: String?
        get() = Localization["editor.trackChange"] + " [LIGHT_GRAY](${Engine.MIN_TRACK_COUNT}≦[]${engine.trackCount}[LIGHT_GRAY]≦${Engine.MAX_TRACK_COUNT})[]\n" +
                Localization[if (engine.canIncreaseTrackCount()) "editor.trackChange.increase" else "editor.trackChange.max"] + "\n" +
                Localization[if (!engine.canDecreaseTrackCount()) "editor.trackChange.min" else if (engine.eventsTouchTrackTop) "editor.trackChange.impedance" else "editor.trackChange.decrease"]
        set(_) {}

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_track_change"))
        })

        this.tooltipTextIsLocalizationKey = false
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (engine.playState == PlayState.STOPPED && engine.canIncreaseTrackCount()) {
            editor.mutate(TrackResizeAction(engine.trackCount, engine.trackCount + 1))
            engine.recomputeCachedData()
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        if (engine.playState == PlayState.STOPPED && engine.canDecreaseTrackCount()) {
            if (!engine.wouldEventsFitNewTrackCount(engine.trackCount - 1)) {
                // Jump to first blocking event
                val events = engine.events.filterNot { it is EndEvent }
                        .filter { (it.bounds.y + it.bounds.height).roundToInt() >= engine.trackCount }.takeUnless(List<Event>::isEmpty) ?: return

                if (!(editor.selection.containsAll(events) && editor.selection.size == events.size)) {
                    editor.mutate(EventSelectionAction(editor.selection.toList(), events))
                }
                editor.renderer.cameraPan = CameraPan(editor.renderer.trackCamera.position.x, events.first().bounds.x, 0.5f, Interpolation.exp10Out)
            } else {
                editor.mutate(TrackResizeAction(engine.trackCount, engine.trackCount - 1))
                engine.recomputeCachedData()
            }
        }
    }
}