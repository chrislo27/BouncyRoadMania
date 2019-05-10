package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.GradientDirection
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EditorRenderer(val editor: Editor) {

    companion object {
        private val TMP_MATRIX = Matrix4()
    }

    private val main: BRManiaApp get() = editor.main
    private val engine: Engine get() = editor.engine

    val trackCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 15f * (16f / 9f), 15f)
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
        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = staticCamera.combined
        batch.begin()
        with(engine) {
            if (gradientDirection == GradientDirection.VERTICAL) {
                batch.drawQuad(0f, 0f, gradientFirst, staticCamera.viewportWidth, 0f, gradientFirst, staticCamera.viewportWidth, staticCamera.viewportHeight, gradientLast, 0f, staticCamera.viewportHeight, gradientLast)
            } else {
                batch.drawQuad(0f, 0f, gradientFirst, staticCamera.viewportWidth, 0f, gradientLast, staticCamera.viewportWidth, staticCamera.viewportHeight, gradientLast, 0f, staticCamera.viewportHeight, gradientFirst)
            }
        }
        batch.end()
        batch.projectionMatrix = TMP_MATRIX
    }

    fun getDebugString(): String {
        return ""
    }

}