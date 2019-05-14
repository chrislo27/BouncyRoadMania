package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.CameraPan
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.gdxutils.*


class TimelineStage(val editor: Editor, parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    val engine: Engine get() = editor.engine

    val minimap: Minimap

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
        }

        minimap = Minimap(this, this)
        elements += minimap

        // Bottom separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 1f, 0f, pixelHeight = 1f)
        }
    }

    inner class Minimap(parent: UIElement<EditorScreen>?, parameterStage: Stage<EditorScreen>?)
        : UIElement<EditorScreen>(parent, parameterStage) {

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (isMouseOver() && (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT)) {
                return (editor.editMode != EditMode.EVENTS || button != Input.Buttons.RIGHT)
            }

            return super.touchDown(screenX, screenY, pointer, button)
        }

        override fun canBeClickedOn(): Boolean {
            return true
        }

        override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            val furthest: Event? = engine.events.maxBy { it.bounds.x + it.bounds.width }
            val maxX: Float = if (furthest == null) editor.renderer.trackCamera.viewportWidth else Math.min(
                    (furthest.bounds.x + furthest.bounds.width).coerceAtLeast(0f), engine.duration)

            shapeRenderer.prepareStencilMask(batch) {
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)
                begin(ShapeRenderer.ShapeType.Filled)
                rect(location.realX - 1, location.realY, location.realWidth + 1, location.realHeight)
                end()
            }.useStencilMask {
                val x = location.realX
                val y = location.realY
                val pxHeight = location.realHeight
                val pxWidth = location.realWidth
                val unitHeight = pxHeight / engine.trackCount
                val unitWidth = pxWidth / maxX

                // events
                engine.events.forEach { event ->
                    batch.color = event.getRenderColor(editor, editor.theme)
                    batch.fillRect(x + event.bounds.x * unitWidth, y + event.bounds.y * unitHeight,
                            event.bounds.width * unitWidth, event.bounds.height * unitHeight)
                }

                // horizontal lines
                batch.color = editor.theme.trackLine
                for (i in 1..engine.trackCount) {
                    batch.fillRect(x, y + i * unitHeight - 0.5f, pxWidth, 1f)
                }
                batch.fillRect(x - 0.5f, y, 1f, pxHeight)
                batch.fillRect(x + pxWidth - 0.5f, y, 1f, pxHeight)
                batch.setColor(1f, 1f, 1f, 1f)

                // trackers
                editor.engine.trackers.forEach { tc ->
                    tc.map.values.forEach { tracker ->
                        batch.color = editor.theme.getTrackerColour(tracker)
                        batch.fillRect(x + tracker.beat * unitWidth - 0.5f, y, 1f, pxHeight)
                    }
                }
                run {
                    batch.color = editor.theme.trackers.musicStart
                    batch.fillRect(x + engine.musicStartSec * unitWidth - 0.5f, y, 1f,
                            pxHeight)
                    val playback = if (engine.playState == PlayState.STOPPED) engine.playbackStart else engine.beat
                    batch.color = editor.theme.trackers.playback
                    batch.fillRect(x + playback * unitWidth - 0.5f, y, 1f, pxHeight)
                }

                // camera box
                batch.setColor(1f, 1f, 1f, 1f)
                val camera = editor.renderer.trackCamera
                if (editor.editMode == EditMode.EVENTS) {
                    batch.drawRect(x + (camera.position.x - camera.viewportWidth / 2) * unitWidth, y,
                            camera.viewportWidth * unitWidth, pxHeight, 2f)
                }

                if (isMouseOver() && engine.playState == PlayState.STOPPED) {
                    if ((editor.clickOccupation == ClickOccupation.None && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) ||
                            (editor.clickOccupation is ClickOccupation.Playback && Gdx.input.isButtonPressed(Input.Buttons.RIGHT))) {
                        val percent = (stage.camera.getInputX() - location.realX) / location.realWidth
                        val endX = percent * maxX
                        val cameraPan = editor.renderer.cameraPan
                        val interpolationX = Interpolation.exp10Out
                        val duration = 0.25f
                        val startX = camera.position.x
                        if (editor.editMode == EditMode.EVENTS) {
                            if (cameraPan == null) {
                                editor.renderer.cameraPan = CameraPan(startX, endX, duration, interpolationX)
                            } else if (cameraPan.endX != endX) {
                                editor.renderer.cameraPan = CameraPan(startX, endX, (duration * (1f - cameraPan.progress)).coerceAtLeast(duration * 0.5f), interpolationX)
                            }
                        } else {
                            editor.renderer.trackCamera.position.x = endX
                        }
                    } else if (editor.clickOccupation == ClickOccupation.None && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && editor.editMode == EditMode.ENGINE) {
                        editor.clickOccupation = ClickOccupation.Playback(editor)
                    }
                }
            }

            if (editor.clickOccupation is ClickOccupation.Playback) {
                val percent = (stage.camera.getInputX() - location.realX) / location.realWidth
                val time = editor.engine.playbackStart
                val signedSec = engine.tempos.beatsToSeconds(time)
                val sec = Math.abs(signedSec)
                val seconds = (if (signedSec < 0) "-" else "") +
                        Editor.TRACKER_MINUTES_FORMATTER.format((sec / 60).toLong()) + ":" + Editor.TRACKER_TIME_FORMATTER.format(sec % 60.0)
                val text = Localization["tracker.playback"] + "\n" + Localization["tracker.any.time", Editor.THREE_DECIMAL_PLACES_FORMATTER.format(time.toDouble()), seconds]
                val font = editor.main.defaultBorderedFont
                font.scaleMul(0.75f)
                font.color = editor.theme.trackers.playback
                val textWidth = font.getTextWidth(text)
                val x = (percent * location.realWidth + location.realX).coerceIn(location.realX + textWidth / 2, location.realX + location.realWidth - textWidth / 2)
                font.draw(batch, text, x, location.realY - font.capHeight, 0f, Align.center, false)
                font.setColor(1f, 1f, 1f, 1f)
                font.scaleMul(1f / 0.75f)
            }

        }
    }

}
