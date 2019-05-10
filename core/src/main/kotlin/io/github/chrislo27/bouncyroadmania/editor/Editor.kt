package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.InputProcessor
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.rendering.EditorRenderer
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.toolboks.i18n.Localization
import kotlin.properties.Delegates


class Editor(val main: BRManiaApp) : InputProcessor {

    var editMode: EditMode by Delegates.observable(EditMode.EVENTS) { _, oldVal, newVal ->
        stage.decideVisibility()
        updateMessageBar()
    }
    val stage: EditorStage = EditorStage(this)
    val engine: Engine = Engine()
    var currentTool: Tool by Delegates.observable(Tool.SELECTION) { _, _, _ -> updateMessageBar() }

    val renderer: EditorRenderer = EditorRenderer(this)

    init {
        Localization.addListener {
            updateMessageBar()
        }
    }

    fun updateMessageBar() {
        val message = StringBuilder()
        val controls = StringBuilder()

        when (editMode) {
            EditMode.PARAMETERS -> message.append(Localization["editor.msg.parameters"])
            else -> {}
        }

        stage.messageLabel.text = message.toString()
        stage.controlsLabel.text = controls.toString()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
}