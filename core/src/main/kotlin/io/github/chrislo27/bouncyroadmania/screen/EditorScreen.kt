package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.toolboks.ToolboksScreen
import org.lwjgl.opengl.GL20


class EditorScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, EditorScreen>(main) {

    val editor: Editor = Editor(main)
    override val stage: EditorStage
        get() = editor.stage
    
    private var lastWindowListener: Lwjgl3WindowListener? = null

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = main.batch

        editor.renderer.render(batch)

        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        editor.renderUpdate()
    }

    override fun show() {
        super.show()
        (Gdx.input.inputProcessor as? InputMultiplexer)?.addProcessor(editor)
        val window = (Gdx.graphics as Lwjgl3Graphics).window
        lastWindowListener = window.windowListener
        window.windowListener = editor
    }

    override fun hide() {
        super.hide()
        (Gdx.input.inputProcessor as? InputMultiplexer)?.removeProcessor(editor)
        (Gdx.graphics as Lwjgl3Graphics).window.windowListener = lastWindowListener
    }

    override fun getDebugString(): String? {
        return editor.getDebugString()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}