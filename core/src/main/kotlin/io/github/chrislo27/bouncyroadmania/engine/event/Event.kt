package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.util.RectanglePool


abstract class Event(val engine: Engine) {

    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle(0f, 0f, 0f, 1f)
    val lerpDifference: Rectangle = Rectangle()
    open val isRendered: Boolean = true
    open val canBeCopied: Boolean = true

    open fun render(batch: SpriteBatch, editor: Editor) {

    }

    abstract fun copy(): Event

    open fun updateInterpolation(forceUpdate: Boolean) {
        if (forceUpdate) {
            lerpDifference.set(0f, 0f, 0f, 0f)
            return
        }

        val delta: Float = Gdx.graphics.deltaTime
        val speedX: Float = 32f
        val speedY: Float = speedX
        val alphaX: Float = (delta * speedX).coerceAtMost(1f)
        val alphaY: Float = (delta * speedY).coerceAtMost(1f)

        lerpDifference.x = MathUtils.lerp(lerpDifference.x, 0f, alphaX)
        lerpDifference.y = MathUtils.lerp(lerpDifference.y, 0f, alphaY)
        lerpDifference.width = MathUtils.lerp(lerpDifference.width, 0f, alphaX)
        lerpDifference.height = MathUtils.lerp(lerpDifference.height, 0f, alphaY)
    }

    open fun onBoundsChange(old: Rectangle) {
        lerpDifference.x = (old.x + lerpDifference.x) - bounds.x
        lerpDifference.y = (old.y + lerpDifference.y) - bounds.y
        lerpDifference.width = (old.width + lerpDifference.width) - bounds.width
        lerpDifference.height = (old.height + lerpDifference.height) - bounds.height
    }

    open fun inRenderRange(start: Float, end: Float): Boolean {
        return bounds.x + lerpDifference.x + bounds.width + lerpDifference.width >= start
                && bounds.x + lerpDifference.x <= end
    }

    /**
     * Automatically calls onBoundsChange and caches the old rectangle.
     */
    inline fun updateBounds(func: () -> Unit) {
        RectanglePool.use { rect ->
            rect.set(bounds)
            func()
            onBoundsChange(rect)
        }
    }

    protected fun SpriteBatch.setColorWithTintIfNecessary(selectionTint: Color, r: Float, g: Float, b: Float, a: Float,
                                                          necessary: Boolean = isSelected) {
        if (necessary) {
            this.setColor((r * (1 + selectionTint.r)).coerceIn(0f, 1f),
                    (g * (1 + selectionTint.g)).coerceIn(0f, 1f),
                    (b * (1 + selectionTint.b)).coerceIn(0f, 1f),
                    a)
        } else {
            this.setColor(r, g, b, a)
        }
    }

    protected fun SpriteBatch.setColorWithTintIfNecessary(selectionTint: Color, color: Color,
                                                          necessary: Boolean = isSelected) {
        this.setColorWithTintIfNecessary(selectionTint, color.r, color.g, color.b, color.a, necessary)
    }

}