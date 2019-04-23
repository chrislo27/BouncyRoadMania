package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class AssetRegistryLoadingScreen(main: BRManiaApp)
    : ToolboksScreen<BRManiaApp, AssetRegistryLoadingScreen>(main) {

    private val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, BRMania.WIDTH * 1f, BRMania.HEIGHT * 1f)
    }
    private var nextScreen: (() -> ToolboksScreen<*, *>?)? = null

    override fun render(delta: Float) {
        super.render(delta)
        val progress = AssetRegistry.load(delta)

        val cam = camera
        val batch = main.batch
        batch.projectionMatrix = cam.combined

        batch.setColor(1f, 1f, 1f, 1f)

        val width = cam.viewportWidth * 0.75f
        val height = cam.viewportHeight * 0.05f
        val line = height / 8f

        batch.begin()

        batch.fillRect(cam.viewportWidth * 0.5f - width * 0.5f,
                       cam.viewportHeight * 0.5f - (height) * 0.5f,
                       width * progress, height)
        batch.drawRect(cam.viewportWidth * 0.5f - width * 0.5f - line * 2,
                       cam.viewportHeight * 0.5f - (height) * 0.5f - line * 2,
                       width + (line * 4), height + (line * 4),
                       line)

        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined

        if (progress >= 1f) {
            main.screen = nextScreen?.invoke()
        }
    }

    fun setNextScreen(next: (() -> ToolboksScreen<*, *>?)?): AssetRegistryLoadingScreen {
        nextScreen = next
        return this
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}