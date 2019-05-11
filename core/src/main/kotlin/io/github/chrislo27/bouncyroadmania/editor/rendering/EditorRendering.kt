package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignature
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth


fun EditorRenderer.renderBeatNumbers(batch: SpriteBatch, beatRange: IntRange, font: BitmapFont) {
    val width = Editor.EVENT_WIDTH * 0.4f
    val y = engine.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS + Editor.TRACK_LINE_THICKNESS) + font.capHeight
    // Render quarter note beat numbers/lines
    for (i in beatRange) {
        val x = i - width / 2f
        val text = if (i == 0) Editor.ZERO_BEAT_SYMBOL else "${Math.abs(i)}"
        font.color = theme.trackLine

        font.drawCompressed(batch, text,
                x, y, width, Align.center)
        if (i < 0) {
            val textWidth = font.getTextWidth(text, width, false)
            font.drawCompressed(batch, Editor.NEGATIVE_SYMBOL, x - textWidth / 2f, y, Editor.EVENT_WIDTH * 0.2f, Align.right)
        }
    }

    // Render measure based beat numbers
    val minInterval = engine.timeSignatures.map.values.minBy { it.noteFraction }?.noteFraction
            ?: (4f / TimeSignature.NOTE_UNITS.last())
    var i = MathHelper.snapToNearest(beatRange.first.toFloat(), minInterval)
    var lastMeasureRendered = -1
    while (i <= MathHelper.snapToNearest(beatRange.last.toFloat(), minInterval)) {
        val x = i - width / 2f
        val measureNum = engine.timeSignatures.getMeasure(i)
        if (measureNum >= 1 && measureNum != lastMeasureRendered && engine.timeSignatures.getMeasurePart(i) == 0 && i < engine.duration) {
            lastMeasureRendered = measureNum
            font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
            font.drawCompressed(batch, "$measureNum",
                    x, y + font.lineHeight, width, Align.center)
        }
        i += minInterval
    }

    font.setColor(1f, 1f, 1f, 1f)
}

fun EditorRenderer.renderBeatLines(batch: SpriteBatch, beatRange: IntRange, trackYOffset: Float, updateDelta: Boolean) {
    for (i in beatRange) {
        batch.color = theme.trackLine
        if (engine.timeSignatures.getMeasurePart(i.toFloat()) > 0) {
            batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.25f)
        }

        val xOffset = toScaleX(Editor.TRACK_LINE_THICKNESS) / -2
        batch.fillRect(i.toFloat() + xOffset, trackYOffset, toScaleX(Editor.TRACK_LINE_THICKNESS),
                engine.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))

        val flashAnimation = subbeatSection.flashAnimation > 0
        val actuallyInRange = (subbeatSection.enabled && i.toFloat() in subbeatSection.start..subbeatSection.end)
        if (flashAnimation || actuallyInRange) {
            batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b,
                    theme.trackLine.a * 0.3f *
                            if (!actuallyInRange) subbeatSection.flashAnimation else 1f)
            for (j in 1 until Math.round(1f / editor.snap)) {
                batch.fillRect(i.toFloat() + editor.snap * j + xOffset, trackYOffset, toScaleX(Editor.TRACK_LINE_THICKNESS),
                        engine.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))
            }
        }
    }

    // Render measure based beat numbers
    val minInterval = 4f / TimeSignature.NOTE_UNITS.last()
    var i = MathHelper.snapToNearest(beatRange.first.toFloat(), minInterval)
    var lastMeasurePtRendered = -1
    var lastMeasureRendered = -1
    while (i <= MathHelper.snapToNearest(beatRange.last.toFloat(), minInterval)) {
        val measurePart = engine.timeSignatures.getMeasurePart(i)
        val measure = engine.timeSignatures.getMeasure(i)
        if (lastMeasurePtRendered != measurePart || lastMeasureRendered != measure) {
            lastMeasurePtRendered = measurePart
            lastMeasureRendered = measure
            if (measurePart > 0) {
                batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.2f)
            } else {
                batch.color = theme.trackLine
            }

            val thickness = Editor.TRACK_LINE_THICKNESS * (if (measurePart == 0) 3 else 1)
            val xOffset = toScaleX(thickness) / -2
            batch.fillRect(i + xOffset, trackYOffset, toScaleX(thickness),
                    engine.trackCount + toScaleY(Editor.TRACK_LINE_THICKNESS))
        }
        i += minInterval
    }

    batch.setColor(1f, 1f, 1f, 1f)

    if (subbeatSection.flashAnimation > 0 && updateDelta) {
        subbeatSection.flashAnimation -= Gdx.graphics.deltaTime / subbeatSection.flashAnimationSpeed
        if (subbeatSection.flashAnimation < 0)
            subbeatSection.flashAnimation = 0f
    }
}

fun EditorRenderer.renderHorizontalTrackLines(batch: SpriteBatch, startX: Float, width: Float, trackYOffset: Float) {
    batch.color = theme.trackLine
    for (i in 0..engine.trackCount) {
        batch.fillRect(startX, trackYOffset + i.toFloat(), width, toScaleY(Editor.TRACK_LINE_THICKNESS))
    }
    batch.setColor(1f, 1f, 1f, 1f)
}
