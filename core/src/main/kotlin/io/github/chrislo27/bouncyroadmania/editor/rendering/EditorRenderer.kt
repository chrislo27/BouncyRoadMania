package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine


class EditorRenderer(val editor: Editor) {

    private val main: BRManiaApp get() = editor.main
    private val engine: Engine get() = editor.engine

    fun render(batch: SpriteBatch) {
        when (editor.editMode) {
            EditMode.ENGINE -> renderEngine(batch)
            EditMode.EVENTS -> renderEvents(batch)
            EditMode.PARAMETERS -> renderParams(batch)
        }
    }

    private fun renderEvents(batch: SpriteBatch) {

    }

    private fun renderEngine(batch: SpriteBatch) {
        engine.render(batch)
    }

    private fun renderParams(batch: SpriteBatch) {

    }

}