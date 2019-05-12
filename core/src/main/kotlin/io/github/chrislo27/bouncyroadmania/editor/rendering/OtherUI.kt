package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*


fun EditorRenderer.renderOtherUI(batch: SpriteBatch, shapeRenderer: ShapeRenderer, beatRange: IntRange, font: BitmapFont) {
    val clickOccupation = editor.clickOccupation
    val camera = this.trackCamera
    when (clickOccupation) {
        is ClickOccupation.SelectionDrag -> {
            val oldColor = batch.packedColor
            val y = 0f
            val mouseY = camera.getInputY()
            val alpha = (1f + y - mouseY).coerceIn(0.5f + MathHelper.getTriangleWave(2f) * 0.125f, 1f)
            val left = camera.position.x - camera.viewportWidth / 2 * camera.zoom

            batch.setColor(1f, 0f, 0f, 0.25f * alpha)
            batch.fillRect(left, y,
                    camera.viewportWidth * camera.zoom,
                    -camera.viewportHeight * camera.zoom)
            batch.packedColor = oldColor

            val deleteFont = main.defaultFontLarge
            deleteFont.scaleFont(camera)
            deleteFont.scaleMul(0.5f)

            deleteFont.setColor(0.75f, 0.5f, 0.5f, alpha)

            deleteFont.drawCompressed(batch, Localization["editor.delete"], left, y + -1f + font.capHeight / 2,
                    camera.viewportWidth * camera.zoom, Align.center)

            deleteFont.setColor(1f, 1f, 1f, 1f)
            deleteFont.unscaleFont()

        }
        is ClickOccupation.CreatingSelection -> {
            val oldColor = batch.packedColor
            val rect = clickOccupation.rectangle
            val selectionFill = theme.selection.fill
            val selectionMode = editor.getSelectionMode()

            batch.setColor(selectionFill.r, selectionFill.g, selectionFill.b, selectionFill.a * 0.85f)
            shapeRenderer.prepareStencilMask(batch) {
                this.begin(ShapeRenderer.ShapeType.Filled)
                engine.events.forEach {
                    if (selectionMode.wouldEventBeIncluded(it, rect, engine.events, editor.selection)) {
                        this.rect(it.bounds.x, it.bounds.y, it.bounds.width, it.bounds.height)
                    }
                }
                this.end()
            }.useStencilMask {
                batch.fillRect(camera.position.x - camera.viewportWidth / 2 * camera.zoom, camera.position.y - camera.viewportHeight / 2 * camera.zoom, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom)
            }

            batch.color = selectionFill
            batch.fillRect(rect)

            batch.color = theme.selection.border
            batch.drawRect(rect, toScaleX(Editor.SELECTION_BORDER), toScaleY(Editor.SELECTION_BORDER))

            run text@{
                val oldFontColor = font.color
                font.color = theme.selection.border

                val toScaleX = toScaleX(Editor.SELECTION_BORDER * 1.5f)
                val toScaleY = toScaleY(Editor.SELECTION_BORDER * 1.5f)
                val shift = Gdx.input.isShiftDown()
                val control = Gdx.input.isControlDown()

                val bigFont = main.defaultFontLarge
                val oldBigFontColor = bigFont.color
                bigFont.scaleFont(camera)

                // AND or XOR strings
                if (rect.height - toScaleY * 2 >= bigFont.capHeight
                        && !(shift && control) && (shift || control)) {
                    bigFont.color = theme.selection.border
                    bigFont.color.a *= 0.25f * MathHelper.getTriangleWave(2f) + 0.35f
                    bigFont.drawCompressed(batch, if (shift) Editor.SELECTION_RECT_ADD else Editor.SELECTION_RECT_INVERT,
                            rect.x + toScaleX, rect.y + rect.height / 2 + bigFont.capHeight / 2,
                            rect.width - toScaleX * 2, Align.center)
                }

                // dimension strings
                if (rect.height - toScaleY * 2 >= font.capHeight) {
                    font.color = theme.trackLine

                    val widthStr = Editor.TWO_DECIMAL_PLACES_FORMATTER.format(rect.width.toDouble())
                    var defaultX = rect.x + toScaleX
                    var defaultWidth = rect.width - toScaleX * 2
                    if (defaultX < camera.position.x - camera.viewportWidth / 2 * camera.zoom) {
                        defaultX = camera.position.x - camera.viewportWidth / 2 * camera.zoom
                        defaultWidth = (rect.width + rect.x) - defaultX - toScaleX
                    } else if (defaultX + defaultWidth > camera.position.x + camera.viewportWidth / 2) {
                        defaultWidth = (camera.position.x + camera.viewportWidth / 2) - defaultX
                    }
                    if (rect.width - toScaleX * 2 >= font.getTextWidth(widthStr) && rect.height - toScaleY * 2 >= font.getTextHeight(widthStr)) {
                        font.drawCompressed(batch, widthStr,
                                defaultX,
                                rect.y + rect.height - toScaleY,
                                defaultWidth, Align.center)
                    }
                }

                bigFont.unscaleFont()
                bigFont.color = oldBigFontColor
                font.color = oldFontColor
            }

            batch.packedColor = oldColor
        }
        else -> {
        }
    }
}
