package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.Tool
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignature
import io.github.chrislo27.bouncyroadmania.util.RectanglePool
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlin.math.roundToInt

private fun EditorRenderer.renderTimeSignature(batch: SpriteBatch, beat: Float, lowerText: String, upperText: String, bigFont: BitmapFont, heightOfTrack: Float) {
    val x = beat
    val startY = 0f + toScaleY(Editor.TRACK_LINE_THICKNESS)
    val maxWidth = 1f

    val lowerWidth = bigFont.getTextWidth(lowerText, maxWidth, false).coerceAtMost(maxWidth)
    val upperWidth = bigFont.getTextWidth(upperText, maxWidth, false).coerceAtMost(maxWidth)
    val biggerWidth = Math.max(lowerWidth, upperWidth)

    bigFont.drawCompressed(batch, lowerText,
            x + biggerWidth * 0.5f - lowerWidth * 0.5f,
            startY + bigFont.capHeight,
            maxWidth, Align.left)
    bigFont.drawCompressed(batch, upperText,
            x + biggerWidth * 0.5f - upperWidth * 0.5f,
            startY + heightOfTrack,
            maxWidth, Align.left)
}

fun EditorRenderer.renderTimeSignatures(batch: SpriteBatch, beatRange: IntRange) {
    val timeSignatures = engine.timeSignatures
    val bigFont = main.timeSignatureFont
    val heightOfTrack = engine.trackCount.toFloat() - toScaleY(Editor.TRACK_LINE_THICKNESS) * 2f
    val inputX = trackCamera.getInputX()
    val inputBeat = Math.floor(inputX.toDouble() / editor.snap).toFloat() * editor.snap
    bigFont.scaleFont(trackCamera)
    bigFont.scaleMul((heightOfTrack * 0.5f - 0.075f * (heightOfTrack / Engine.DEFAULT_TRACK_COUNT)) / bigFont.capHeight)

    timeSignatures.map.values.forEach { timeSig ->
        if (timeSig.beat.roundToInt() !in beatRange) return@forEach
        if (editor.currentTool == Tool.TIME_SIGNATURE && MathUtils.isEqual(timeSig.beat, inputBeat) && engine.playState == PlayState.STOPPED) {
            bigFont.color = theme.selection.border
        } else {
            bigFont.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.75f)
        }

        renderTimeSignature(batch, timeSig.beat, timeSig.lowerText, timeSig.upperText, bigFont, heightOfTrack)
    }

    if (editor.currentTool == Tool.TIME_SIGNATURE && engine.timeSignatures.map[inputBeat] == null && engine.playState == PlayState.STOPPED) {
        bigFont.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * MathUtils.lerp(0.2f, 0.35f, MathHelper.getTriangleWave(2f)))
        val last = engine.timeSignatures.getTimeSignature(inputBeat)
        renderTimeSignature(batch, inputBeat, last?.lowerText ?: TimeSignature.DEFAULT_NOTE_UNIT.toString(), last?.upperText ?: TimeSignature.DEFAULT_NOTE_UNIT.toString(), bigFont, heightOfTrack)
    }

    bigFont.setColor(1f, 1f, 1f, 1f)
    bigFont.unscaleFont()
}

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

fun EditorRenderer.renderStripeBoard(batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
    val clickOccupation = editor.clickOccupation
    if (clickOccupation is ClickOccupation.SelectionDrag) {
        val oldColor = batch.packedColor
        val rect = RectanglePool.obtain()
        rect.set(clickOccupation.lerpLeft, clickOccupation.lerpBottom, clickOccupation.lerpRight - clickOccupation.lerpLeft, clickOccupation.lerpTop - clickOccupation.lerpBottom)

        if ((!clickOccupation.isPlacementValid() || clickOccupation.isInDeleteZone())) {
            batch.setColor(1f, 0f, 0f, 0.15f)
            batch.fillRect(rect)

            val camera = trackCamera
            shapeRenderer.projectionMatrix = camera.combined
            shapeRenderer.prepareStencilMask(batch) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
                shapeRenderer.end()
            }.useStencilMask {
                val tex = AssetRegistry.get<Texture>("ui_stripe_board")
                val scale = 2f
                val w = tex.width.toFloat() / BRMania.WIDTH * camera.viewportWidth / scale
                val h = tex.height.toFloat() / BRMania.HEIGHT * camera.viewportHeight / scale
                for (x in 0..(BRMania.WIDTH / tex.width * scale).roundToInt() + 2) {
                    for (y in 0..(BRMania.HEIGHT / tex.height * scale).roundToInt() + 2) {
                        batch.draw(tex, x * w - camera.viewportWidth / 2 * camera.zoom + camera.position.x, y * h - camera.viewportHeight / 2 * camera.zoom + camera.position.y, w, h)
                    }
                }
            }
            batch.setColor(1f, 0f, 0f, 0.5f)
            batch.drawRect(rect, toScaleX(Editor.SELECTION_BORDER) * 2, toScaleY(Editor.SELECTION_BORDER) * 2)
        }

        batch.packedColor = oldColor
        RectanglePool.free(rect)
    }
}
