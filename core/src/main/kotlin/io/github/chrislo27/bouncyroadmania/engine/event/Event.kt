package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.util.RectanglePool
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.*


abstract class Event(val engine: Engine) {

    companion object {
        const val STRETCH_AREA: Float = 1f / 12f
        const val MIN_STRETCH: Float = 1f / 8f
        const val BORDER: Float = 4f
    }

    var playbackCompletion: PlaybackCompletion = PlaybackCompletion.WAITING
    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle(0f, 0f, 0f, 1f)
    val lerpDifference: Rectangle = Rectangle()
    open val isRendered: Boolean = true
    open val canBeCopied: Boolean = true
    open val isStretchable: Boolean = false
    open val hasEditableParams: Boolean = false
    open val renderText: String = ""
    var needsNameTooltip: Boolean = false
        protected set

    init {
        this.bounds.width = 1f
    }

    open fun getRenderColor(editor: Editor, theme: EditorTheme): Color {
        return theme.events.generic
    }

    open fun render(batch: SpriteBatch, editor: Editor) {
        val textColor = editor.theme.events.nameText
        val text = renderText
        val font = editor.main.defaultFont
        val color = getRenderColor(editor, editor.theme)
        val oldColor = batch.packedColor
        val oldFontSizeX = font.data.scaleX
        val oldFontSizeY = font.data.scaleY
        val selectionTint = editor.theme.events.selectionTint
        val showSelection = isSelected

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        // filled rect + border
        batch.setColorWithTintIfNecessary(selectionTint, color.r, color.g, color.b, color.a, necessary = showSelection)
        batch.fillRect(x, y,
                width, height)

        batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                (color.g - 0.25f).coerceIn(0f, 1f),
                (color.b - 0.25f).coerceIn(0f, 1f),
                color.a, necessary = showSelection)

        if (this.isStretchable) {
            val oldColor2 = batch.packedColor
            val arrowWidth: Float = Math.min(width / 2f, Editor.EVENT_HEIGHT / Editor.EVENT_WIDTH)
            val y2 = y + height / 2 - 0.5f
            val arrowTex = AssetRegistry.get<Texture>("event_stretchable_arrow")

            batch.setColorWithTintIfNecessary(selectionTint, (color.r - 0.25f).coerceIn(0f, 1f),
                    (color.g - 0.25f).coerceIn(0f, 1f),
                    (color.b - 0.25f).coerceIn(0f, 1f),
                    color.a * 0.5f, necessary = showSelection)

            batch.draw(arrowTex, x + arrowWidth, y2, width - arrowWidth * 2, 1f,
                    arrowTex.width / 2, 0, arrowTex.width / 2,
                    arrowTex.height, false, false)
            batch.draw(arrowTex, x, y2, arrowWidth, 1f,
                    0, 0, arrowTex.width / 2, arrowTex.height, false, false)
            batch.draw(arrowTex, x + width, y2, -arrowWidth, 1f,
                    0, 0, arrowTex.width / 2, arrowTex.height, false, false)

            batch.packedColor = oldColor2
        }

        batch.drawRect(x, y,
                width, height,
                editor.renderer.toScaleX(BORDER), editor.renderer.toScaleY(BORDER))

        renderBeforeText(editor, batch)

        batch.packedColor = oldColor
        val oldFontColor = font.color
        val fontScale = 0.6f
        font.color = textColor
        font.data.setScale(oldFontSizeX * fontScale, oldFontSizeY * fontScale)
        // width - iconSizeX - 6 * (editor.toScaleX(BORDER))
        val allottedWidth = width - 2 * (editor.renderer.toScaleX(BORDER))
        val allottedHeight = height - 4 * (editor.renderer.toScaleY(BORDER))

        val textHeight = font.getTextHeight(text, allottedWidth, true)
        val textX = x + 1 * (editor.renderer.toScaleX(BORDER))
        val textY = y + height / 2
        if (textHeight > allottedHeight) {
            val ratio = Math.min(allottedWidth / (font.getTextWidth(text, allottedWidth, false)), allottedHeight / textHeight)
            font.data.setScale(ratio * font.data.scaleX, ratio * font.data.scaleY)
        }
        needsNameTooltip = textHeight > allottedHeight
        var newTextWidth = allottedWidth
        val camera = editor.renderer.trackCamera
        val outerBound = camera.position.x + camera.viewportWidth / 2 * camera.zoom
        if (textX + newTextWidth > outerBound) {
            newTextWidth = (outerBound) - textX
        }
        newTextWidth = newTextWidth.coerceAtLeast(font.getTextWidth(text)).coerceAtMost(allottedWidth)

        font.draw(batch, text, textX, textY + font.getTextHeight(text, newTextWidth, true) / 2, newTextWidth, Align.right, true)

//        when (editor.scrollMode) {
//            Editor.ScrollMode.PITCH -> {
//                if (this is IRepitchable && (this.canBeRepitched || this.semitone != 0)) {
//                    drawCornerText(editor, batch, getTextForSemitone(semitone), !this.canBeRepitched, x, y)
//                }
//            }
//            Editor.ScrollMode.VOLUME -> {
//                if (this is IVolumetric && (this.isVolumetric || this.volumePercent != IVolumetric.DEFAULT_VOLUME)) {
//                    drawCornerText(editor, batch, IVolumetric.getVolumeText(this.volumePercent), !this.isVolumetric, x, y)
//                }
//            }
//        }
        font.color = oldFontColor
        font.data.setScale(oldFontSizeX, oldFontSizeY)
    }

    abstract fun copy(): Event

    open fun onStart() {

    }

    open fun whilePlaying() {

    }

    open fun onEnd() {

    }

    open fun createParamsStage(editor: Editor, stage: EditorStage): EventParamsStage<*> =
            if (hasEditableParams) error("This function should be overridden")
            else throw NotImplementedError("Not implemented for events without editable params")

    open fun getLowerUpdateableBound(): Float = bounds.x
    open fun getUpperUpdateableBound(): Float = bounds.maxX

    open fun isUpdateable(beat: Float): Boolean {
        return beat in getLowerUpdateableBound()..getUpperUpdateableBound()
    }

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

    protected open fun renderBeforeText(editor: Editor, batch: SpriteBatch) {

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