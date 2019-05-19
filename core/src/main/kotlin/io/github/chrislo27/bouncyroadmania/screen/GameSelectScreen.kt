package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.discord.DiscordHelper
import io.github.chrislo27.bouncyroadmania.discord.PresenceState
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.unpack
import io.github.chrislo27.bouncyroadmania.util.*
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.zip.ZipFile


class GameSelectScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, GameSelectScreen>(main), Lwjgl3WindowListener {

    companion object {
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("Faster Does It") }
    }

    sealed class LoadState {
        interface IsWorking
        object None : LoadState()
        object Leaving : LoadState(), IsWorking
        object WaitingForChooser : LoadState(), IsWorking
        object Loading : LoadState(), IsWorking
        class Loaded(val engine: Engine) : LoadState(), Disposable {
            override fun dispose() {
                engine.dispose()
            }
        }
    }

    private var lastWindowListener: Lwjgl3WindowListener? = null
    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    override val stage: Stage<GameSelectScreen> = Stage(null, main.defaultCamera, camera.viewportWidth, camera.viewportHeight)
    val music: Music by lazy {
        AssetRegistry.get<Music>("music_play_screen").apply {
            isLooping = true
        }
    }

    val label: TextLabel<GameSelectScreen>
    val backButton: Button<GameSelectScreen>
    val playButton: Button<GameSelectScreen>
    val openButton: Button<GameSelectScreen>
    val openDiffButton: Button<GameSelectScreen>
    @Volatile
    private var loadState: LoadState = LoadState.None
        set(value) {
            (field as? Disposable)?.dispose()
            field = value
            backButton.enabled = value !is LoadState.IsWorking
            playButton.visible = false
            openButton.visible = value !is LoadState.IsWorking
            openDiffButton.visible = false
        }

    init {
        val palette = main.uiPalette
        stage.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }
        stage.elements += ColourPane(stage, stage).apply {
            this.colour.set(1f, 1f, 1f, 1f)
        }
        stage.elements += object : Button<GameSelectScreen>(palette, stage, stage) {
            val unmuted = TextureRegion(AssetRegistry.get<Texture>("ui_music"))
            val muted = TextureRegion(AssetRegistry.get<Texture>("ui_music_muted"))
            val label = ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            }

            init {
                addLabel(label)
            }
        }.apply {
            this.location.set(screenWidth = 0f, screenHeight = 0f,
                    pixelWidth = 32f, pixelHeight = 32f, pixelX = camera.viewportWidth - 32f, pixelY = camera.viewportHeight - 32f)
            val muted = main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)
            music.volume = if (muted) 0f else 1f
            label.image = if (muted) this.muted else unmuted
            this.leftClickAction = { _, _ ->
                val old = main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)
                main.preferences.putBoolean(PreferenceKeys.MUTE_MUSIC, !old).flush()
                music.volume = if (!old) 0f else 1f
                label.image = if (!old) this@apply.muted else unmuted
                this.tooltipText = "mainMenu.tooltip.${if (old) "mute" else "unmute"}Music"
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.tooltip.${if (!muted) "mute" else "unmute"}Music"
        }
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenX = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = -450f - 4f, pixelWidth = 450f, pixelHeight = 16f * 3)
            this.addLabel(TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = MUSIC_CREDIT
                this.textAlign = Align.right
                this.textWrapping = false
                this.fontScaleMultiplier = 0.5f
            })
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("https://incompetech.com")
            }
        }
        stage.elements += ColourPane(stage, stage).apply {
            this.colour.set(palette.backColor)
            this.location.set(screenX = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = -4f, pixelWidth = 4f, pixelHeight = 16f * 3)
        }
        
        stage.elements += TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), stage, stage).apply {
            this.location.set(screenX = 0f, screenWidth = 0f, screenHeight = 0f, pixelX = 0f, pixelWidth = 220f, pixelHeight = 70f)
            this.isLocalizationKey = false
            this.text = " Game Controls:\n [YELLOW]\uE0E0[] - J\n [RED]\uE110[] - D"
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.background = true
        }

        label = TextLabel(palette.copy(), stage, stage).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.location.set(screenY = 0.225f, screenHeight = 0.725f)
            this.text = Localization["play.select"]
            this.background = true
        }
        stage.elements += label
        playButton = Button(palette.copy(highlightedBackColor = Color(0.125f, 0.5f, 0.125f, 0.75f), clickedBackColor = Color(0f, 0.75f, 0.5f, 0.75f)), stage, stage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "play.playButton"
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                
            }
            this.location.set(screenX = 0.3f, screenWidth = 0.4f, screenY = 0.1f, screenHeight = 0.1f)
            this.visible = false
        }
        stage.elements += playButton
        openButton = Button(palette, stage, stage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "play.openButton"
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                GlobalScope.launch {
                    load(null)
                }
            }
            this.location.set(screenX = 0.3f, screenWidth = 0.4f, screenY = 0.1f, screenHeight = 0.1f)
            this.visible = true
        }
        stage.elements += openButton
        openDiffButton = Button(palette, stage, stage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "play.openDifferentButton"
                this.fontScaleMultiplier = 0.85f
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                GlobalScope.launch {
                    load(null)
                }
            }
            this.location.set(screenX = 0.025f, screenWidth = 0.25f, screenY = 0.1f, screenHeight = 0.1f)
            this.visible = false
        }
        stage.elements += openDiffButton

        backButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 1f, screenWidth = 0f, screenHeight = 0f, pixelWidth = 32f, pixelHeight = 32f, pixelY = -32f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_back"))
            })
            this.leftClickAction = { _, _ ->
                backToMainMenu()
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "play.back"
        }
        stage.elements += backButton

        stage.updatePositions()
    }

    init {
        music.volume = if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) 0f else 1f
        music.play()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            backToMainMenu()
        }
    }

    private fun backToMainMenu() {
        if (loadState !is LoadState.IsWorking) {
            loadState = LoadState.Leaving
            main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
        }
    }

    private fun load(initialFile: File?) {
        if (loadState !is LoadState.IsWorking) {
            val filter = TinyFDWrapper.Filter(listOf("*.${BRMania.FILE_EXTENSION}"), Localization["save.fileFilter"] + " (.${BRMania.FILE_EXTENSION})")
            loadState = if (initialFile == null) LoadState.WaitingForChooser else LoadState.Loading
            if (initialFile == null) {
                Gdx.app.postRunnable {
                    label.text = Localization["closeChooser"]
                }
            }
            val file = initialFile ?: TinyFDWrapper.openFile(Localization["load.fileChooserTitle"], attemptRememberDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_LOAD)?.absolutePath?.plus("/"), false, filter)
            if (file != null && file.exists()) {
                val zipFile = ZipFile(file)
                try {
                    Gdx.app.postRunnable {
                        label.text = Localization["load.loading"]
                    }
                    val newEngine = Engine()
                    newEngine.unpack(zipFile)
                    persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_LOAD, file.parentFile)
                    System.gc()
                    Gdx.app.postRunnable {
                        loadState = LoadState.Loaded(newEngine)
                        label.text = Localization["play.readyToPlay", file.name]
                        playButton.visible = true
                        openDiffButton.visible = true
                        openButton.visible = false
                    }
                } catch (e: Exception) {
                    loadState = LoadState.None
                    e.printStackTrace()
                    Gdx.app.postRunnable {
                        label.text = Localization["load.failed", e::class.java.canonicalName]
                    }
                } finally {
                    StreamUtils.closeQuietly(zipFile)
                }
            } else {
                Gdx.app.postRunnable {
                    loadState = LoadState.None
                    label.text = Localization["play.select"]
                }
            }
        }
    }

    override fun filesDropped(files: Array<out String>) {
        if (loadState !is LoadState.IsWorking) {
            val path = files.firstOrNull { it.toLowerCase(Locale.ROOT).endsWith("." + BRMania.FILE_EXTENSION.toLowerCase(Locale.ROOT)) }
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    GlobalScope.launch {
                        load(file)
                    }
                }
            }
        }
    }

    override fun closeRequested(): Boolean {
        return true
    }

    override fun maximized(isMaximized: Boolean) {
    }

    override fun created(window: Lwjgl3Window?) {
    }

    override fun focusLost() {
    }

    override fun focusGained() {
    }

    override fun refreshRequested() {
    }

    override fun iconified(isIconified: Boolean) {
    }

    override fun show() {
        super.show()
        music.play()
        val window = (Gdx.graphics as Lwjgl3Graphics).window
        lastWindowListener = window.windowListener
        window.windowListener = this
        DiscordHelper.updatePresence(PresenceState.GameSelect)
    }

    override fun hide() {
        super.hide()
        music.fadeTo(0f, 0.25f)
        (Gdx.graphics as Lwjgl3Graphics).window.windowListener = lastWindowListener
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}