package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.Tool
import io.github.chrislo27.bouncyroadmania.registry.EventRegistry
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask
import kotlin.math.absoluteValue


class PickerDisplay(parent: PickerStage, val editor: Editor, val number: Int = 5) : UIElement<EditorScreen>(parent, parent) {

    companion object {
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
    }

    var index: Int = 0
        set(value) {
            val list = EventRegistry.list
            field = if (list.isEmpty())
                0
            else
                value.coerceIn(0, list.size - 1)
            (parent as PickerStage).updateLabels()
        }
    var renderedIndex: Float = 0f

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val list = EventRegistry.list
        if (list.isEmpty())
            return

        renderedIndex = MathUtils.lerp(renderedIndex, index.toFloat(), Gdx.graphics.deltaTime / 0.075f)

        shapeRenderer.prepareStencilMask(batch) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.rect(location.realX, location.realY, location.realWidth, location.realHeight)
            shapeRenderer.end()
        }.useStencilMask {
            val font = editor.main.kurokaneBorderedFont
            font.scaleMul(0.25f)
            val sectionY = location.realHeight / number
            val currentTool = editor.currentTool
            list.forEachIndexed { index, item ->
                val half = number / 2
                if ((renderedIndex - index).absoluteValue > half + 1) {
                    return@forEachIndexed
                }

                val selected = index == this.index
                font.color = if (selected && currentTool == Tool.SELECTION) SELECTED_TINT else Color.WHITE
                val availableWidth = location.realWidth
                val drawX = location.realX
                val drawY = location.realY + location.realHeight / 2 + sectionY * (renderedIndex - index)
                val shouldUseNewAlpha = currentTool != Tool.SELECTION
                val newAlpha = 0.4f

                drawTextWithAlpha(batch, font, item.displayName, drawX, drawY + font.capHeight / 2, availableWidth, Align.left, false, newAlpha, shouldUseNewAlpha)
            }

            font.scaleMul(1f / 0.25f)
            font.setColor(1f, 1f, 1f, 1f)
        }
    }

    private fun drawTextWithAlpha(batch: SpriteBatch, font: BitmapFont, text: String, drawX: Float, drawY: Float, availableWidth: Float, align: Int, wrap: Boolean, alpha: Float, setAlpha: Boolean) {
        val textWidth = font.getTextWidth(text)
        val oldScaleX = font.data.scaleX
        if (textWidth > availableWidth) {
            font.data.scaleX = (availableWidth / textWidth) * oldScaleX
        }
        font.cache.also { cache ->
            cache.clear()
            cache.addText(text, drawX, drawY, availableWidth, align, wrap)
            if (setAlpha) {
                cache.setAlphas(alpha)
            }
            cache.draw(batch)
        }
        font.data.scaleX = oldScaleX
    }

    override fun scrolled(amount: Int): Boolean {
        val list = EventRegistry.list
        if (list.isNotEmpty() && isMouseOver()) {
            val newIndex = index + amount
            index = newIndex.coerceIn(0, list.size - 1)
            return true
        }
        return false
    }
}