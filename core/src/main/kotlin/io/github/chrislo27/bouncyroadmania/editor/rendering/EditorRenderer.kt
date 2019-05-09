package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EditorRenderer(val editor: Editor) {

    companion object {
        private val TMP_MATRIX = Matrix4()
    }

    private val main: BRManiaApp get() = editor.main
    private val engine: Engine get() = editor.engine

    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val staticCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
        update()
    }

    fun render(batch: SpriteBatch) {
        val theme = main.editorTheme

        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = staticCamera.combined
        batch.begin()
        batch.color = theme.background
        batch.fillRect(0f, 0f, staticCamera.viewportWidth, staticCamera.viewportHeight)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
        batch.projectionMatrix = TMP_MATRIX

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