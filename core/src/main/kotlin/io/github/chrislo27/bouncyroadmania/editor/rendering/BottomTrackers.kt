package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.Tool
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.Swing
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlin.math.roundToInt


fun EditorRenderer.renderBottomTrackers(batch: SpriteBatch, beatRange: IntRange) {
    val borderedFont = main.defaultBorderedFont
    borderedFont.scaleFont(trackCamera)

    val triHeight = 0.5f
    val triWidth = toScaleX(triHeight * Editor.EVENT_HEIGHT)
    val triangle = AssetRegistry.get<Texture>("tracker_tri")
    val rightTriangle = AssetRegistry.get<Texture>("tracker_right_tri_bordered")
    val tool = editor.currentTool
    val clickOccupation = editor.clickOccupation
    val toolIsTrackerBased = tool.isTrackerRelated
    val clickIsTrackerResize = clickOccupation is ClickOccupation.TrackerResize
    val currentTracker: Tracker<*>? = editor.getTrackerOnMouse(tool.trackerClass?.java
            ?: (if (tool == Tool.SWING) TempoChange::class.java else null), true)

    fun renderTracker(layer: Int, color: Color, text: String, beat: Float, width: Float, slope: Int) {
        val heightPerLayer = 0.75f
        val y = 0f - (layer + 1) * heightPerLayer
        val height = 0f - y

        // background
        batch.setColor(color.r, color.g, color.b, color.a * 0.35f)
        val batchColor = batch.packedColor
        val fadedColor = Color.toFloatBits(color.r, color.g, color.b, color.a * 0.025f)
        if (slope == 0 || width == 0f) {
            batch.fillRect(beat, y, width, height)
        } else {
            if (slope == -1) {
                batch.drawQuad(beat, y + height, batchColor,
                        beat + width, y + height, fadedColor,
                        beat + width, y, fadedColor,
                        beat, y, batchColor)
            } else if (slope == 1) {
                batch.drawQuad(beat, y + height, fadedColor,
                        beat + width, y + height, batchColor,
                        beat + width, y, batchColor,
                        beat, y, fadedColor)
            }
        }

        batch.drawRect(beat, y, width, height, toScaleX(2f), toScaleY(2f))

        // lines
        batch.color = color
        val lineWidth = toScaleX(Editor.TRACK_LINE_THICKNESS)
        batch.fillRect(beat, y, lineWidth, height)
        batch.fillRect(beat + width, y, -lineWidth, height)

        // triangle
        if (width == 0f) {
            batch.draw(triangle, beat - triWidth * 0.5f, y, triWidth, triHeight)
        } else {
            batch.draw(rightTriangle, beat + width, y, -triWidth * 0.75f, triHeight)
            batch.draw(rightTriangle, beat, y, triWidth * 0.75f, triHeight)
        }

        // text
        borderedFont.color = color
        borderedFont.drawCompressed(batch, text, beat + triWidth * 0.5f,
                y + heightPerLayer * 0.5f + borderedFont.capHeight * 0.5f,
                2f, Align.left)

        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun getColorForTracker(tracker: Tracker<*>): Color {
        return if (currentTracker === tracker && engine.playState == PlayState.STOPPED && !clickIsTrackerResize && (toolIsTrackerBased || (tool == Tool.SWING && currentTracker is TempoChange))) Color.WHITE else theme.getTrackerColour(tracker)
    }

    fun Tracker<*>.render() {
        renderTracker(container.renderLayer, getColorForTracker(this), text, beat, width, getSlope())
    }

    engine.trackersReverseView.forEach { container ->
        container.map.values.forEach {
            if ((clickOccupation !is ClickOccupation.TrackerResize || clickOccupation.tracker !== it) && it !== currentTracker && (it.endBeat.roundToInt() >= beatRange.first && it.beat.roundToInt() <= beatRange.last)) {
                it.render()
            }
        }
    }

    if (clickOccupation is ClickOccupation.TrackerResize) {
        renderTracker(clickOccupation.renderLayer,
                if (clickOccupation.isPlacementValid()) Color.WHITE else Color.RED,
                clickOccupation.text,
                clickOccupation.beat, clickOccupation.width, clickOccupation.tracker.getSlope())
    } else {
        currentTracker?.render()
    }

    // Swing indicators
    borderedFont.setColor(1f, 1f, 1f, 1f)
    run {
        val tempos = engine.tempos
        var lastSwing: Swing = tempos.defaultSwing
        tempos.map.values.forEach {
            if ((it.endBeat.roundToInt() >= beatRange.first && it.beat.roundToInt() <= beatRange.last) && (lastSwing != it.swing)) {
                val noteSymbol: String = it.swing.getNoteSymbol()
                val swingName: String = it.swing.getSwingName()

                if (tool == Tool.SWING) {
                    borderedFont.color = getColorForTracker(it)
                } else {
                    borderedFont.setColor(1f, 1f, 1f, 1f)
                }
                val y = engine.trackCount + 1f
                borderedFont.drawCompressed(batch, swingName, it.beat, y, 2f, Align.left)

                val lh = borderedFont.capHeight * 1.1f

                borderedFont.scaleMul(0.75f)
                borderedFont.drawCompressed(batch, "${it.swing.ratio}%, $noteSymbol", it.beat, y + lh, 2f, Align.left)
                borderedFont.scaleMul(1 / 0.75f)
            }

            lastSwing = it.swing
        }
    }

    borderedFont.setColor(1f, 1f, 1f, 1f)
    borderedFont.unscaleFont()
}