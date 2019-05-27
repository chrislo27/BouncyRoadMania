package io.github.chrislo27.bouncyroadmania.editor.stage.initialparams

import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class MetadataStage(ipStage: InitialParamsStage, palette: UIPalette)
    : CategoryStage(ipStage, palette, "initialParams.category.metadata") {

    val version: TextLabel<EditorScreen>
    val difficultyDown: Button<EditorScreen>
    val difficultyUp: Button<EditorScreen>
    val difficulty: TextLabel<EditorScreen>
    
    init {
        val labelPalette = palette.copy(ftfont = ipStage.editor.main.defaultBorderedFontFTF)
        version = TextLabel(palette, this, this).apply {
            this.location.set(pixelX = -(40f * 15 + 8f), screenX = 1f, screenWidth = 0f, pixelWidth = 40f * 15, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textAlign = Align.center
            this.background = true
        }
        elements += version
        
        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0f, pixelWidth = 40f * 10, screenHeight = 0f, screenY = 1f, pixelY = -40f * 1, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.metadata.recommendedDifficulty"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        difficultyDown = Button(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0f, pixelWidth = 40f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "▼"
                this.isLocalizationKey = false
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                engine.difficulty--
                updateLabels()
            }
        }
        elements += difficultyDown
        difficulty = TextLabel(palette, this, this).apply {
            this.location.set(pixelX = 8f + 40f, screenX = 0f, screenWidth = 0f, pixelWidth = 40f * 8, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textAlign = Align.center
            this.background = true
            
        }
        elements += difficulty
        difficultyUp = Button(palette, this, this).apply {
            this.location.set(pixelX = 8f + 40f * 9, screenX = 0f, screenWidth = 0f, pixelWidth = 40f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "▲"
                this.isLocalizationKey = false
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                engine.difficulty++
                updateLabels()
            }
        }
        elements += difficultyUp
        
        updateLabels()
    }

    fun updateLabels() {
        difficultyDown.enabled = engine.difficulty > Engine.MIN_DIFFICULTY
        difficultyUp.enabled = engine.difficulty < Engine.MAX_DIFFICULTY
        difficulty.text = "[YELLOW]${"★".repeat(engine.difficulty)}[][GRAY]${"★".repeat(Engine.MAX_DIFFICULTY - engine.difficulty)}[]"
        version.text = Localization["initialParams.metadata.version", engine.version.toString()]
    }

    override fun onEngineChange(engine: Engine) {
        updateLabels()
    }
    
}