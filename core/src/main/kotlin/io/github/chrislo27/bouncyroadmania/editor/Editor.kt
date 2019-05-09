package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.InputProcessor
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.rendering.EditorRenderer
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import kotlin.properties.Delegates


class Editor(val main: BRManiaApp) : InputProcessor {

    val stage: EditorStage = EditorStage(this)
    var editMode: EditMode by Delegates.observable(EditMode.EVENTS) { _, oldVal, newVal ->

    }
    val engine: Engine = Engine()
    var currentTool: Tool = Tool.SELECTION

    val renderer: EditorRenderer = EditorRenderer(this)

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