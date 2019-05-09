package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.toolboks.ToolboksScreen
import org.lwjgl.opengl.GL20


class EditorScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, EditorScreen>(main) {

    val editor: Editor = Editor(main)
    override val stage: EditorStage
        get() = editor.stage

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = main.batch

        editor.renderer.render(batch)

        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        // FIXME
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = MainMenuScreen(main)
        }
    }

    override fun show() {
        super.show()
        (Gdx.input.inputProcessor as? InputMultiplexer)?.addProcessor(editor)
    }

    override fun hide() {
        super.hide()
        (Gdx.input.inputProcessor as? InputMultiplexer)?.removeProcessor(editor)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}