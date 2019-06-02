package io.github.chrislo27.bouncyroadmania.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.input.InputResult
import io.github.chrislo27.bouncyroadmania.engine.input.InputScore
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement


class TimingDisplayStage<S : ToolboksScreen<*, *>>(parent: UIElement<S>, camera: OrthographicCamera, val alpha: Float = 0.75f)
    : Stage<S>(parent, camera) {

    companion object {
        val ACE_COLOUR: Color = Color.valueOf("FFF800")
        val GOOD_COLOUR: Color = Color.valueOf("6DE23B")
        val BARELY_COLOUR: Color = Color.valueOf("FF7C26")
        val MISS_COLOUR: Color = Color.valueOf("E82727")
    }

    data class Flash(val offset: Float, val timing: InputResult, val startDuration: Float, var duration: Float = startDuration)

    private val flashes: MutableList<Flash> = mutableListOf()
    private val texRegionGreat: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("ui_input_timing"), 0, 0, 128, 128)
    private val texRegionMiss: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("ui_input_timing"), 128, 0, 128, 128)
    private val texRegionBarely: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("ui_input_timing"), 256, 0, 128, 128)

    init {
        // Border
        this.elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, alpha)
            this.location.set(screenWidth = 0f, pixelX = -1f, pixelY = -1f, pixelHeight = 2f, pixelWidth = 1f)
        }
        this.elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, alpha)
            this.location.set(screenWidth = 0f, screenX = 1f, pixelX = 0f, pixelY = -1f, pixelHeight = 2f, pixelWidth = 1f)
        }
        this.elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, alpha)
            this.location.set(screenHeight = 0f, pixelX = 0f, pixelY = -1f, pixelHeight = 1f)
        }
        this.elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, alpha)
            this.location.set(screenY = 1f, screenHeight = 0f, pixelX = 0f, pixelY = 0f, pixelHeight = 1f)
        }
        fun addColourPane(color: Color, x: Float, percentage: Float) {
            val actualWidth = percentage / 2f   
            this.elements += ColourPane(this, this).apply {
                this.colour.set(color)
                this.colour.a *= alpha
                this.location.set(screenX = x / 2, screenWidth = actualWidth)
            }
            this.elements += ColourPane(this, this).apply {
                this.colour.set(color)
                this.colour.a *= alpha
                this.location.set(screenX = 1f - x / 2 - actualWidth, screenWidth = actualWidth)
            }
        }

        val acePercentage = (Engine.ACE_OFFSET) / Engine.MAX_OFFSET_SEC
        val goodPercentage = (Engine.GOOD_OFFSET - Engine.ACE_OFFSET) / Engine.MAX_OFFSET_SEC
        val barelyPercentage = (Engine.BARELY_OFFSET - Engine.GOOD_OFFSET) / Engine.MAX_OFFSET_SEC
        val missPercentage = (Engine.MAX_OFFSET_SEC - Engine.BARELY_OFFSET) / Engine.MAX_OFFSET_SEC
        addColourPane(MISS_COLOUR, 0f, missPercentage)
        addColourPane(BARELY_COLOUR, missPercentage, barelyPercentage)
        addColourPane(GOOD_COLOUR, missPercentage + barelyPercentage, goodPercentage)
        addColourPane(ACE_COLOUR, missPercentage + barelyPercentage + goodPercentage, acePercentage)
    }

    fun flash(input: InputResult) {
        val offsetNormalized: Float = (input.accuracySec / Engine.MAX_OFFSET_SEC).coerceIn(-1f, 1f)
        if (input.inputScore == InputScore.MISS && offsetNormalized <= (Engine.BARELY_OFFSET / Engine.MAX_OFFSET_SEC)) return
        flashes += Flash(offsetNormalized, input, 0.25f)
    }
    
    fun clearFlashes() {
        flashes.clear()
    }

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)

        flashes.forEach { flash ->
            batch.setColor(1f, 1f, 1f, Interpolation.pow5Out.apply(flash.duration / flash.startDuration))
            val texReg: TextureRegion = when(flash.timing.inputScore) {
                InputScore.MISS -> texRegionMiss
                InputScore.ACE, InputScore.GOOD -> texRegionGreat
                else -> texRegionBarely
            }
            val squareSize = location.realHeight
            batch.draw(texReg, location.realX + (location.realWidth / 2) * (flash.offset + 1f) - squareSize / 2,
                    location.realY, squareSize, squareSize)

        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun frameUpdate(screen: S) {
        super.frameUpdate(screen)

        flashes.forEach {
            it.duration -= Gdx.graphics.deltaTime
        }
        flashes.removeIf { it.duration <= 0f }
    }
}