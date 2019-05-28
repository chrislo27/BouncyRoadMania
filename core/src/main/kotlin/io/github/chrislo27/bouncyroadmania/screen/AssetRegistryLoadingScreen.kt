package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul


class AssetRegistryLoadingScreen(main: BRManiaApp)
    : ToolboksScreen<BRManiaApp, AssetRegistryLoadingScreen>(main) {

    companion object {
        private val INTRO_LENGTH = 0.985f
    }

    private val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, BRMania.WIDTH * 1f, BRMania.HEIGHT * 1f)
    }
    //    private var nextScreen: (() -> ToolboksScreen<*, *>?)? = null
    private var finishCallback: () -> Unit = {}

    private var finishedLoading = false
    private var transition: Float = 0f

    private var lastProgress = 0f

    private lateinit var mainMenuScreen: MainMenuScreen
    private lateinit var transitionScreen: TransitionScreen<BRManiaApp>
    private var lastTitleX: Float = -1f
    private var lastTitleY: Float = -1f

    override fun render(delta: Float) {
        super.render(delta)
        lastProgress = AssetRegistry.load(delta)

        val cam = camera
        val batch = main.batch
        batch.projectionMatrix = cam.combined

        batch.setColor(1f, 1f, 1f, 1f)

        val width = cam.viewportWidth * 0.75f
        val height = cam.viewportHeight * 0.05f
        val line = height / 8f

        batch.begin()

        val progress = lastProgress
        batch.fillRect(cam.viewportWidth * 0.5f - width * 0.5f,
                cam.viewportHeight * 0.3f - (height) * 0.5f,
                width * progress, height)
        batch.drawRect(cam.viewportWidth * 0.5f - width * 0.5f - line * 2,
                cam.viewportHeight * 0.3f - (height) * 0.5f - line * 2,
                width + (line * 4), height + (line * 4),
                line)

        // title
        if (main.screen === this) {
            val titleFont = main.cometBorderedFont
            titleFont.scaleFont(camera)
            titleFont.scaleMul(0.6f)
            val textW = titleFont.getTextWidth(BRMania.TITLE)
            if (lastTitleX == -1f) {
                lastTitleX = camera.viewportWidth / 2f - textW / 2
            }
            if (lastTitleY == -1f) {
                lastTitleY = camera.viewportHeight / 2f + titleFont.capHeight / 2f
            }
            titleFont.draw(batch, BRMania.TITLE, lastTitleX, lastTitleY)
            titleFont.unscaleFont()
        }

        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (lastProgress >= 1f) {
            if (!finishedLoading) {
                finishedLoading = true
                finishCallback()
                AssetRegistry.get<Sound>("sfx_main_menu_intro").play(if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) 0f else 1f)
                mainMenuScreen = MainMenuScreen(main).apply {
                    this.music.stop()
                    this.hideTitle = true
                }
                transitionScreen = object : TransitionScreen<BRManiaApp>(main, this@AssetRegistryLoadingScreen, mainMenuScreen, WipeTo(Color.BLACK, 0.25f), WipeFrom(Color.BLACK, 0.25f)) {
                    override fun render(delta: Float) {
                        super.render(delta)
                        val batch = main.batch
                        batch.projectionMatrix = camera.combined
                        batch.begin()
                        val titleFont = main.cometBorderedFont
                        titleFont.scaleFont(camera)
                        titleFont.scaleMul(0.6f)
                        val titleX = Interpolation.smooth.apply(lastTitleX, mainMenuScreen.titleXStart, percentageTotal)
                        val titleY = Interpolation.smooth.apply(lastTitleY, mainMenuScreen.menuTop + titleFont.lineHeight, percentageTotal)
                        titleFont.draw(batch, BRMania.TITLE, titleX, titleY)
                        titleFont.unscaleFont()
                        batch.end()
                        batch.projectionMatrix = main.defaultCamera.combined
                    }
                }
            } else {
                transition += Gdx.graphics.deltaTime
                if (transition >= INTRO_LENGTH) {
                    mainMenuScreen.music.play()
                    main.screen = transitionScreen
                }
            }
        }
    }

//    fun setNextScreen(next: (() -> ToolboksScreen<*, *>?)?): AssetRegistryLoadingScreen {
//        nextScreen = next
//        return this
//    }

    fun onFinishLoading(next: () -> Unit): AssetRegistryLoadingScreen {
        finishCallback = next
        return this
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}