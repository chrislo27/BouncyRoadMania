package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.initialparams.InitialParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.EngineEventListener
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class EditorStage(val editor: Editor)
    : Stage<EditorScreen>(null, editor.main.defaultCamera, 1280f, 720f), EngineEventListener {
    
    val palette = BRManiaApp.instance.uiPalette
    val pickerHeight = 160f
    
    val toolbarStage: ToolbarStage
    val timelineStage: TimelineStage
    val messageBarStage: Stage<EditorScreen>
    val pickerStage: PickerStage
    val initialParamsStage: InitialParamsStage
    
    val messageLabel: TextLabel<EditorScreen>
    val controlsLabel: TextLabel<EditorScreen>
    
    var paramsStage: ParamsStage? = null
        private set

    var isTyping: Boolean = false
        private set

    init {
        toolbarStage = ToolbarStage(this, palette).apply {
            this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -40f, pixelHeight = 40f)
        }
        elements += toolbarStage

        timelineStage = TimelineStage(editor, this, palette).apply {
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
            this.location.set(screenWidth = 1f, screenHeight = 0f, pixelY = messageBarHeight, pixelHeight = pickerHeight)
        }
        elements += pickerStage
        
        initialParamsStage = InitialParamsStage(editor, this, palette).apply {
            this.location.set(screenWidth = 1f, screenHeight = 0f, pixelY = messageBarHeight, pixelHeight = 648f)
        }
        elements += initialParamsStage

        this.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
        }

        stage.updatePositions()
        decideVisibility()
    }
    
    fun setParamsStage(paramsStage: ParamsStage?) {
        if (this.paramsStage != null) {
            stage.removeChild(this.paramsStage!!)
            stage.updatePositions()
        }
        this.paramsStage = paramsStage
        if (paramsStage != null) {
            stage.elements += paramsStage
            val width = 0.35f
            paramsStage.location.set(screenX = 1f - width, screenWidth = width, screenHeight = 0f, screenY = 0f, pixelY = pickerStage.location.pixelY + pickerHeight)
            paramsStage.location.set(pixelHeight = 640f - paramsStage.location.pixelY)
            paramsStage.updatePositions()
        }
        stage.updatePositions()
    }

    override fun onPlayStateChanged(oldState: PlayState, newState: PlayState) {
        paramsStage?.takeIf { it.mustCloseWhenPlaying }?.visible = newState == PlayState.STOPPED
    }

    override fun onEventAdded(event: Event) {
    }

    override fun onEventRemoved(event: Event) {
        val p = paramsStage
        if (p != null && p is EventParamsStage<*> && p.event == event) {
            setParamsStage(null)
        }
    }
    
    fun onEngineChange(engine: Engine) {
        initialParamsStage.onEngineChange(engine)
    }
    
    fun onEditModeChanged(editMode: EditMode) {
        decideVisibility()
        setParamsStage(null)
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
        toolbarStage.tapalongButton.enabled = editor.editMode == EditMode.EVENTS
        initialParamsStage.visible = editor.editMode == EditMode.PARAMETERS
        messageBarStage.visible = editor.editMode != EditMode.ENGINE
    }

}