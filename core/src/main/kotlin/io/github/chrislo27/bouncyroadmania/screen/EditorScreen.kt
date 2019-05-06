package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.toolboks.ToolboksScreen
import org.lwjgl.opengl.GL20


class EditorScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, EditorScreen>(main) {

    val editor: Editor = Editor()
    override val stage: EditorStage
        get() = editor.stage

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        // FIXME
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = MainMenuScreen(main)
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}