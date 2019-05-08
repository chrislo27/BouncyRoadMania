package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class EditorStage(val editor: Editor)
    : Stage<EditorScreen>(null, OrthographicCamera().apply { setToOrtho(false, 1280f, 720f) }, 1280f, 720f) {

    val toolbarStage: ToolbarStage
    val messageBarStage: Stage<EditorScreen>
    
    val messageLabel: TextLabel<EditorScreen>
    val controlsLabel: TextLabel<EditorScreen>

    init {
        val palette = BRManiaApp.instance.uiPalette
        toolbarStage = ToolbarStage(this, palette).apply {
            this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -40f, pixelHeight = 40f)
        }
        elements += toolbarStage

        messageBarStage = Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
            this.location.set(screenHeight = 0f, screenY = 0f, pixelY = 0f, pixelHeight = 32f)
            elements += ColourPane(this, this).apply {
                this.colour.set(0f, 0f, 0f, 0.75f)
            }
        }
        elements += messageBarStage
        messageLabel = TextLabel(palette, messageBarStage, messageBarStage).apply {
            this.fontScaleMultiplier = 0.5f
            this.textWrapping = false
            this.text = ""
            this.isLocalizationKey = false
            this.textAlign = Align.left
            this.location.set(screenHeight = 0.5f)
        }
        messageBarStage.elements += messageLabel
        controlsLabel = TextLabel(palette, messageBarStage, messageBarStage).apply {
            this.fontScaleMultiplier = 0.5f
            this.textWrapping = false
            this.text = ""
            this.isLocalizationKey = false
            this.textAlign = Align.left
            this.location.set(screenHeight = 0.5f, screenY = 0.5f)
        }
        messageBarStage.elements += controlsLabel

        this.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
        }

        stage.updatePositions()
    }

}