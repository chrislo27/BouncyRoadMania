package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.stage.ColourPicker
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import kotlin.math.roundToInt


class TapalongButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    override var tooltipText: String?
        get() = if (editor.editMode == EditMode.EVENTS) "editor.tapalong" else "editor.tapalong.disabled"
        set(_) {}

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_tapalong_button"))
        })
        this.tooltipTextIsLocalizationKey = true
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (editorStage.paramsStage is TapalongStage) {
            editorStage.setParamsStage(null)
        } else {
            editorStage.setParamsStage(TapalongStage(editorStage))
        }
    }
}

class TapalongStage(parent: EditorStage) : ParamsStage(parent) {

    companion object {
        val AUTO_RESET_IN = 5f
        val MAX_INPUTS = 1024
    }

    override val mustCloseWhenPlaying: Boolean = false

    val points: MutableList<Float> = mutableListOf()
    var averageBpm: Float = 0f
        private set
    var standardDeviation: Float = 0f
        private set
    var timeSinceLastTap: Long = System.currentTimeMillis()
        private set
    private var internalTimekeeper: Float = 0f

    val averageLabel: TextLabel<EditorScreen>
    val intAvgLabel: TextLabel<EditorScreen>
    val stdDevLabel: TextLabel<EditorScreen>
    val numLabel: TextLabel<EditorScreen>

    init {
        title.isLocalizationKey = true
        title.text = "editor.tapalong"

        stdDevLabel = TextLabel(palette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenWidth = 0.5f, screenHeight = 0f, pixelWidth = -4f, pixelHeight = 32f, pixelY = 70f)
        }
        contentStage.elements += stdDevLabel
        contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "tapalong.stdDev"
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenWidth = 0.5f, screenHeight = 0f, pixelWidth = -4f, pixelHeight = 32f, pixelY = 102f)
        }
        numLabel = TextLabel(palette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenX = 0.5f, screenHeight = 0f, screenWidth = 0.5f, pixelX = 4f, pixelWidth = -4f, pixelHeight = 32f, pixelY = 70f)
        }
        contentStage.elements += numLabel
        contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "tapalong.numberOfInputs"
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenX = 0.5f, screenHeight = 0f, screenWidth = 0.5f, pixelX = 4f, pixelWidth = -4f, pixelHeight = 32f, pixelY = 102f)
        }
        averageLabel = TextLabel(palette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = ""
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenHeight = 0f, screenX = 0.3f, screenWidth = 0.4f, pixelHeight = 32f, pixelY = 134f + 192f - 32f)
        }
        contentStage.elements += averageLabel
        val bigPalette = palette.copy(ftfont = parent.editor.main.defaultFontLargeFTF, fontScale = 0.75f)
        intAvgLabel = TextLabel(bigPalette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = ""
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenHeight = 0f, screenX = 0.3f, screenWidth = 0.4f, pixelHeight = 64f, pixelY = 134f + 192f)
        }
        contentStage.elements += intAvgLabel
        contentStage.elements += TextLabel(bigPalette, contentStage, contentStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = "♩="
            this.textAlign = Align.right
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenHeight = 0f, screenX = 0f, screenWidth = 0.3f, pixelHeight = 64f, pixelY = 134f + 192f)
        }
//        contentStage.elements += ImageLabel(palette, contentStage, contentStage).apply {
////            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_dog_in_sink"))
//            this.location.set(screenHeight = 0f, screenX = 0.3f, screenWidth = 0.4f, pixelHeight = 134f + 192f - 32f * 2 - 102, pixelY = 102f + 32f)
//        }
        contentStage.elements += ColourPicker(palette, contentStage, contentStage).apply {

            this.location.set(screenHeight = 0f, screenX = 0f, screenWidth = 1f, pixelHeight = 134f + 192f - 32f * 2 - 102, pixelY = 102f + 32f)
        }

        contentStage.elements += Button(palette, contentStage, contentStage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "tapalong.tapButton"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.75f
            })
            this.leftClickAction = { _, _ ->
                tap()
            }
            this.location.set(screenWidth = 0.6f, screenHeight = 0f, pixelWidth = -4f, pixelHeight = 64f)
        }
        contentStage.elements += Button(palette, contentStage, contentStage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "tapalong.reset"
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                reset()
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "tapalong.reset.tooltip"
            this.location.set(screenX = 0.6f, screenWidth = 0.4f, screenHeight = 0f, pixelX = 4f, pixelWidth = -4f, pixelHeight = 64f)
        }

        updateLabels()
    }

    fun tap() {
        if (points.isNotEmpty() && System.currentTimeMillis() - timeSinceLastTap >= 1000 * AUTO_RESET_IN) {
            reset()
        }

        while (points.size >= MAX_INPUTS) {
            points.removeAt(0)
        }

        if (points.none { it == internalTimekeeper }) {
            // The check prevents instantaneous duplicates
            points += internalTimekeeper
        }
        timeSinceLastTap = System.currentTimeMillis()

        if (points.size >= 2) {
            points.sortBy { it }
            val deltas = points.drop(1).mapIndexed { index, rec -> rec - points[index] }
            val avgDelta = deltas.average()

            averageBpm = 60f / avgDelta.toFloat()
            standardDeviation = Math.sqrt(deltas.map {
                val diff = it - avgDelta
                diff * diff
            }.average() * 1000).toFloat()
        }

        updateLabels()
    }

    fun reset() {
        points.clear()
        internalTimekeeper = 0f
        averageBpm = 0f
        standardDeviation = 0f
        updateLabels()
    }

    fun updateLabels() {
        numLabel.text = Localization["tapalong.number", points.size]
        stdDevLabel.text = Localization["tapalong.number", standardDeviation]
        averageLabel.text = if (points.size >= 2) Localization["tapalong.number", averageBpm] else ""
        intAvgLabel.text = if (points.size == 1) "[LIGHT_GRAY]${Localization["tapalong.first"]}[]" else Localization["tapalong.number", averageBpm.roundToInt()]
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        internalTimekeeper += Gdx.graphics.deltaTime
        super.render(screen, batch, shapeRenderer)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (visible) {
            if (keycode == Input.Keys.T) {
                tap()
                return true
            } else if (keycode == Input.Keys.R) {
                reset()
                return true
            }
        }

        return false
    }

}
