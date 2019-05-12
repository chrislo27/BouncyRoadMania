package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class EditorStage(val editor: Editor)
    : Stage<EditorScreen>(null, editor.main.defaultCamera, 1280f, 720f) {

    val toolbarStage: ToolbarStage
    val timelineStage: TimelineStage
    val messageBarStage: Stage<EditorScreen>
    val pickerStage: PickerStage
    
    val messageLabel: TextLabel<EditorScreen>
    val controlsLabel: TextLabel<EditorScreen>

    var isTyping: Boolean = false
        private set

    init {
        val palette = BRManiaApp.instance.uiPalette
        toolbarStage = ToolbarStage(this, palette).apply {
            this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -40f, pixelHeight = 40f)
        }
        elements += toolbarStage

        timelineStage = TimelineStage(this, palette).apply {
            this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -40f * 2f, pixelHeight = 40f)
        }
        elements += timelineStage

        val messageBarHeight = 32f
        messageBarStage = Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
            this.location.set(screenHeight = 0f, screenY = 0f, pixelY = 0f, pixelHeight = messageBarHeight)
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

        pickerStage = PickerStage(editor, this, palette).apply {
            this.location.set(screenWidth = 1f, screenHeight = 0f, pixelY = messageBarHeight, pixelHeight = 160f)
        }
        elements += pickerStage

        this.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
        }

        stage.updatePositions()
        decideVisibility()
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)
        isTyping = checkIsTyping(this.elements)
    }

    private fun checkIsTyping(list: List<UIElement<EditorScreen>>): Boolean {
        return list.any {
            if (it is Stage) {
                checkIsTyping(it.elements)
            } else (it as? TextField)?.hasFocus ?: false
        }
    }

    /**
     * Decides what parts of the stage are visible based on the current editor state.
     */
    fun decideVisibility() {
        timelineStage.visible = editor.editMode != EditMode.PARAMETERS
        pickerStage.visible = editor.editMode == EditMode.EVENTS
    }

}