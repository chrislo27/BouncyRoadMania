package io.github.chrislo27.bouncyroadmania.editor.stage.initialparams

import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class ResultsStage(ipStage: InitialParamsStage, palette: UIPalette)
    : CategoryStage(ipStage, palette, "initialParams.category.results") {

    val titleField: TextField<EditorScreen>
    val firstPosField: TextField<EditorScreen>
    val firstNegField: TextField<EditorScreen>
    val secondPosField: TextField<EditorScreen>
    val secondNegField: TextField<EditorScreen>

    init {
        val labelPalette = palette.copy(ftfont = ipStage.editor.main.defaultBorderedFontFTF)
        
        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 1, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.results.title"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        titleField = TextField(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.canPaste = true
            this.canInputNewlines = true
            this.background = true
        }
        elements += titleField

        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 3, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.results.first.positive"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        firstPosField = TextField(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 4, pixelHeight = 40f)
            this.canPaste = true
            this.canInputNewlines = true
            this.background = true
        }
        elements += firstPosField
        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.5f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 3, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.results.first.negative"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        firstNegField = TextField(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.5f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 4, pixelHeight = 40f)
            this.canPaste = true
            this.canInputNewlines = true
            this.background = true
        }
        elements += firstNegField

        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 5, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.results.second.positive"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        secondPosField = TextField(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 6, pixelHeight = 40f)
            this.canPaste = true
            this.canInputNewlines = true
            this.background = true
        }
        elements += secondPosField
        elements += TextLabel(labelPalette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.5f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 5, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "initialParams.results.second.negative"
            this.textWrapping = false
            this.textAlign = Align.left
        }
        secondNegField = TextField(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.5f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 6, pixelHeight = 40f)
            this.canPaste = true
            this.canInputNewlines = true
            this.background = true
        }
        elements += secondNegField
        
        elements += TextLabel(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.25f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 0f, pixelY = 40f * 1, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "textField.pressEnterToFinish"
            this.textWrapping = false
            this.textAlign = Align.center
            this.background = true
        }
        elements += TextLabel(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0.25f, screenWidth = 0.5f, pixelWidth = -16f, screenHeight = 0f, screenY = 0f, pixelY = 0f, pixelHeight = 40f)
            this.isLocalizationKey = true
            this.text = "textField.enterNewlines"
            this.textWrapping = false
            this.textAlign = Align.center
            this.background = true
        }
        
        onEngineChange(engine)
    }

    override fun onEngineChange(engine: Engine) {
        val resultsText = engine.resultsText
        titleField.text = resultsText.title
        firstPosField.text = resultsText.firstPositive
        firstNegField.text = resultsText.firstNegative
        secondPosField.text = resultsText.secondPositive
        secondNegField.text = resultsText.secondNegative
    }
}