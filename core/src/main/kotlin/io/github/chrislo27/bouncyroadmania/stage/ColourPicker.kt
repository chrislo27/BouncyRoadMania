package io.github.chrislo27.bouncyroadmania.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import java.util.*
import kotlin.math.roundToInt


class ColourPicker<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : Stage<S>(parent, stage.camera, stage.pixelsWidth, stage.pixelsHeight) {

    data class HSV(var hue: Float, var saturation: Float, var value: Float)

    private val tmpColor = Color(1f, 1f, 1f, 1f)
    private val tmpArr = FloatArray(3)
    private val noSat = Color(1f, 1f, 1f, 1f)
    private val maxSat = Color(1f, 1f, 1f, 1f)
    val hsv: HSV = HSV(0f, 1f, 1f)
    val currentColour = Color(1f, 1f, 1f, 1f)

    val hex: TextField<S>
    val display: ColourPane<S>
    val hue: ImageLabel<S>
    val saturation: SatBar
    val value: ValueBar
    val hueField: TextField<S>
    val satField: TextField<S>
    val valueField: TextField<S>
    val hueArrow: MovingArrow
    val satArrow: MovingArrow
    val valueArrow: MovingArrow
    
    var onColourChange: (color: Color) -> Unit = {}

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
        }
        val labelWidth = 0.15f
        // HSV labels
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "H: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = 0.25f, screenY = 0.75f)
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "S: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = 0.25f, screenY = 0.5f)
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "V: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = 0.25f, screenY = 0.25f)
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "# "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = 0.25f, screenY = 0f)
        }

        hue = ImageLabel(palette, this, this).apply {
            this.renderType = ImageLabel.ImageRendering.RENDER_FULL
            this.image = TextureRegion(BRManiaApp.instance.hueBar)
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = 0.15f, screenY = 0.75f + 0.05f)
        }
        elements += hue
        saturation = SatBar(this).apply {
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = 0.15f, screenY = 0.5f + 0.05f)
        }
        elements += saturation
        value = ValueBar(this).apply {
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = 0.15f, screenY = 0.25f + 0.05f)
        }
        elements += value
        hueArrow = MovingArrow(this).apply {
            this.location.set(hue.location)
            this.onPercentageChange = {
                hsv.hue = it * 360f
                onHsvChange(true)
            }
        }
        elements += hueArrow
        satArrow = MovingArrow(this).apply {
            this.location.set(saturation.location)
            this.onPercentageChange = {
                hsv.saturation = it
                onHsvChange(true)
            }
        }
        elements += satArrow
        valueArrow = MovingArrow(this).apply {
            this.location.set(value.location)
            this.onPercentageChange = {
                hsv.value = it
                onHsvChange(true)
            }
        }
        elements += valueArrow

        hueField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.hue = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 360f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = 0.2f, screenY = 0.75f + 0.025f)
        }
        elements += hueField
        satField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.saturation = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 100f)?.div(100f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = 0.2f, screenY = 0.5f + 0.025f)
        }
        elements += satField
        valueField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.value = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 100f)?.div(100f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = 0.2f, screenY = 0.25f + 0.025f)
        }
        elements += valueField

        hex = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                val c = Color.valueOf(this.text.padEnd(6, 'f'))
                setColor(c)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 6
            this.textAlign = Align.left
            this.location.set(screenX = labelWidth, screenWidth = 0.4f, screenHeight = 0.2f, screenY = 0.025f)
        }
        elements += hex

        display = ColourPane(this, this).apply {
            this.location.set(screenX = 0.65f, screenWidth = 0.325f, screenHeight = 0.2f, screenY = 0.025f)
        }
        elements += display

        setColor(Color.WHITE)
    }

    fun setColor(color: Color, triggerListener: Boolean = true) {
        val arr = tmpArr
        color.toHsv(arr)
        hsv.hue = arr[0]
        hsv.saturation = arr[1]
        hsv.value = arr[2]
        onHsvChange(triggerListener)
    }

    private fun onHsvChange(triggerListener: Boolean) {
        hex.text = tmpColor.fromHsv(hsv.hue, hsv.saturation, hsv.value).toString().toUpperCase(Locale.ROOT).take(6)
        currentColour.fromHsv(hsv.hue, hsv.saturation, hsv.value)
        maxSat.fromHsv(hsv.hue, 1f, hsv.value)
        noSat.fromHsv(hsv.hue, 0f, hsv.value)
        display.colour.fromHsv(hsv.hue, hsv.saturation, hsv.value)
        hueField.text = hsv.hue.toInt().coerceIn(0, 360).toString()
        satField.text = (hsv.saturation * 100).roundToInt().coerceIn(0, 100).toString()
        valueField.text = (hsv.value * 100).roundToInt().coerceIn(0, 100).toString()
        // update sliders
        hueArrow.percentage = hsv.hue.coerceIn(0f, 360f) / 360f
        satArrow.percentage = hsv.saturation.coerceIn(0f, 1f)
        valueArrow.percentage = hsv.value.coerceIn(0f, 1f)
        
        if (triggerListener)
            onColourChange(currentColour)
    }

    inner class ValueBar(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            batch.drawQuad(location.realX, location.realY, Color.BLACK, location.realX + location.realWidth, location.realY, currentColour,
                    location.realX + location.realWidth, location.realY + location.realHeight, currentColour,
                    location.realX, location.realY + location.realHeight, Color.BLACK)
        }
    }

    inner class SatBar(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {

        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            batch.drawQuad(location.realX, location.realY, noSat, location.realX + location.realWidth, location.realY, maxSat,
                    location.realX + location.realWidth, location.realY + location.realHeight, maxSat,
                    location.realX, location.realY + location.realHeight, noSat)
        }
    }

    inner class MovingArrow(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        var percentage = 0f
        
        var onPercentageChange: (value: Float) -> Unit = {}
        
        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            if (wasClickedOn && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                val old = percentage
                percentage = ((stage.camera.getInputX() - location.realX) / location.realWidth).coerceIn(0f, 1f)
                if (percentage != old) {
                    onPercentageChange(percentage)
                }
            }
            
            val tex = AssetRegistry.get<Texture>("ui_colour_picker_arrow")
            val height = location.realHeight / 2f
            batch.setColor(1f, 1f, 1f, 1f)
            batch.draw(tex, location.realX + location.realWidth * percentage - height / 2, location.realY,
                    height, height)
        }

        override fun canBeClickedOn(): Boolean {
            return true
        }
    }
}