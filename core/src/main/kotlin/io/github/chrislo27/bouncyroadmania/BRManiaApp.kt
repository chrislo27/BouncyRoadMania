package io.github.chrislo27.bouncyroadmania

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import io.github.chrislo27.bouncyroadmania.discord.DiscordHelper
import io.github.chrislo27.bouncyroadmania.discord.PresenceState
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.engine.entity.BouncerShaders
import io.github.chrislo27.bouncyroadmania.init.InitialAssetLoader
import io.github.chrislo27.bouncyroadmania.screen.AssetRegistryLoadingScreen
import io.github.chrislo27.bouncyroadmania.screen.MainMenuScreen
import io.github.chrislo27.bouncyroadmania.util.JsonHandler
import io.github.chrislo27.bouncyroadmania.util.ReleaseObject
import io.github.chrislo27.toolboks.ResizeAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl
import org.lwjgl.glfw.GLFW
import java.io.File


class BRManiaApp(logger: Logger, logToFile: File?)
    : ToolboksGame(logger, logToFile, BRMania.VERSION, BRMania.WIDTH to BRMania.HEIGHT, ResizeAction.KEEP_ASPECT_RATIO, BRMania.WIDTH to BRMania.HEIGHT) {

    companion object {
        lateinit var instance: BRManiaApp
            private set

        val httpClient: AsyncHttpClient = Dsl.asyncHttpClient(DefaultAsyncHttpClientConfig.Builder().setThreadFactory { Thread(it).apply { isDaemon = true } }.setFollowRedirect(true).setCompressionEnforced(true))

        private const val RAINBOW_STR = "RAINBOW"
        private val rainbowColor: Color = Color()

        init {
            Colors.put("X", Color.CLEAR)
            Colors.put("NOTE", Color.CYAN)
            Colors.put("WARN", Color.ORANGE)
            Colors.put("ERROR", Color.RED)
        }
    }

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin_merged.ttf") }
    private val cometFontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/cometstd.otf") }
    private val kurokaneFontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/kurokanestd.otf") }
    private val fontAfterLoadFunction: FreeTypeFont.() -> Unit = {
        this.font!!.apply {
            setFixedWidthGlyphs("1234567890")
            data.setLineHeight(lineHeight * 0.9f)
            setUseIntegerPositions(true)
            data.markupEnabled = true
            data.missingGlyph = data.getGlyph('☒')
        }
    }

    val uiPalette: UIPalette by lazy {
        UIPalette(defaultFontFTF, defaultFontLargeFTF, 1f,
                Color(1f, 1f, 1f, 1f),
                Color(0f, 0f, 0f, 0.75f),
                Color(0.25f, 0.25f, 0.25f, 0.75f),
                Color(0f, 0.5f, 0.5f, 0.75f))
    }

    // Fonts

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"
    val cometBorderedFontKey = "comet_bordered_font"
    val kurokaneBorderedFontKey = "kurokane_bordered_font"
    val timeSignatureFontKey = "time_signature_font"

    val defaultFontFTF: FreeTypeFont get() = fonts[defaultFontKey]
    val defaultBorderedFontFTF: FreeTypeFont get() = fonts[defaultBorderedFontKey]
    val defaultFontLargeFTF: FreeTypeFont get() = fonts[defaultFontLargeKey]
    val defaultBorderedFontLargeFTF: FreeTypeFont get() = fonts[defaultBorderedFontLargeKey]
    val cometBorderedFontFTF: FreeTypeFont get() = fonts[cometBorderedFontKey]
    val kurokaneBorderedFontFTF: FreeTypeFont get() = fonts[kurokaneBorderedFontKey]
    val timeSignatureFontFTF: FreeTypeFont get() = fonts[timeSignatureFontKey]

    val defaultFontLarge: BitmapFont get() = defaultFontLargeFTF.font!!
    val defaultBorderedFontLarge: BitmapFont get() = defaultBorderedFontLargeFTF.font!!
    val cometBorderedFont: BitmapFont get() = cometBorderedFontFTF.font!!
    val kurokaneBorderedFont: BitmapFont get() = kurokaneBorderedFontFTF.font!!
    val timeSignatureFont: BitmapFont get() = timeSignatureFontFTF.font!!

    // End of Fonts

    lateinit var preferences: Preferences
        private set
    var versionTextWidth: Float = -1f
        private set
    var editorTheme: EditorTheme = EditorTheme.DEFAULT_THEMES.getValue("light")

    private var lastWindowed: Pair<Int, Int> = BRMania.WIDTH to BRMania.HEIGHT
    lateinit var hueBar: Texture
        private set
    lateinit var hsvShader: ShaderProgram
    
    @Volatile
    var githubVersion: Version = Version.RETRIEVING
        private set
    var secondsElapsed: Float = 0f
        private set

    override fun getTitle(): String = "${BRMania.TITLE} ${BRMania.VERSION}"

    override val programLaunchArguments: List<String>
        get() = BRMania.launchArguments

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("${BRMania.TITLE} $versionString is starting...")
        // 1.8.0_144
        // 9.X.Y(extra)
        val javaVersion = System.getProperty("java.version").trim()
        Toolboks.LOGGER.info("Running on JRE $javaVersion")

        instance = this

        val windowHandle = (Gdx.graphics as Lwjgl3Graphics).window.windowHandle
        GLFW.glfwSetWindowAspectRatio(windowHandle, 16, 9)

        // localization stuff
        run {
            Localization.loadBundlesFromLangFile()
            Localization.logMissingLocalizations()
        }

        // font stuff
        run {
            fonts[defaultFontLargeKey] = createDefaultLargeFont()
            fonts[defaultBorderedFontLargeKey] = createDefaultLargeBorderedFont()
            fonts[cometBorderedFontKey] = createCometBorderedFont()
            fonts[kurokaneBorderedFontKey] = createKurokaneBorderedFont()
            fonts[timeSignatureFontKey] = FreeTypeFont(kurokaneFontFileHandle, emulatedSize, createDefaultTTFParameter().apply {
                size *= 6
                characters = "0123456789?_+-!&%"
                incremental = false
            }).setAfterLoad {
                this.font!!.apply {
                    setFixedWidthGlyphs("0123456789")
                }
            }
            fonts.loadUnloaded(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
        }

        AssetRegistry.addAssetLoader(InitialAssetLoader())

        // generate hue bar
        run {
            val pixmap = Pixmap(360, 1, Pixmap.Format.RGBA8888)
            val tmpColor = Color(1f, 1f, 1f, 1f)
            for (i in 0 until 360) {
                tmpColor.fromHsv(i.toFloat(), 1f, 1f)
                pixmap.setColor(tmpColor)
                pixmap.drawPixel(i, 0)
            }
            hueBar = Texture(pixmap).apply {
                this.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        }
        
        hsvShader = ShaderProgram(BouncerShaders.vertex, BouncerShaders.frag)

        DiscordHelper.init()
        DiscordHelper.updatePresence(PresenceState.Loading)

        // screens
        run {
            fun addOtherScreens() {

            }
            this.screen = AssetRegistryLoadingScreen(this).setNextScreen {
                addOtherScreens()
                MainMenuScreen(this)
            }
        }

        // preferences
        preferences = Gdx.app.getPreferences("BouncyRoadMania")
        editorTheme = EditorTheme.DEFAULT_THEMES[preferences.getString("editorTheme", "")] ?: editorTheme
        
        GlobalScope.launch {
            try {
                val nano = System.nanoTime()
                val obj = JsonHandler.fromJson<ReleaseObject>(
                        httpClient.prepareGet(BRMania.RELEASE_API_URL).execute().get().responseBody)

                githubVersion = Version.fromStringOrNull(obj.tag_name ?: "???") ?: Version.UNKNOWN
                Toolboks.LOGGER.info(
                        "Fetched latest version from GitHub in ${(System.nanoTime() - nano) / 1_000_000f} ms, is $githubVersion")

                val v = githubVersion
                if (!v.isUnknown) {
                    if (v > BRMania.VERSION) {
                        preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE,
                                preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0) + 1)
                    } else {
                        preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun preRender() {
        secondsElapsed += Gdx.graphics.deltaTime
        rainbowColor.fromHsv(MathHelper.getSawtoothWave(2f) * 360f, 0.8f, 0.8f)
        Colors.put(RAINBOW_STR, rainbowColor)
        super.preRender()
    }

    override fun postRender() {
//        val screen = screen
//        if (screen !is HidesVersionText || !screen.hidesVersionText) {
//            val font = defaultBorderedFont
//            font.data.setScale(0.5f)
//
//            font.setColor(1f, 1f, 1f, 1f)
//
//            val oldProj = batch.projectionMatrix
//            batch.projectionMatrix = defaultCamera.combined
//            batch.begin()
//            val layout = font.draw(batch, BRMania.VERSION.toString(),
//                                   0f,
//                                   (font.capHeight) + (2f / BRMania.HEIGHT) * defaultCamera.viewportHeight,
//                                   defaultCamera.viewportWidth, Align.right, false)
//            versionTextWidth = layout.width
//            batch.end()
//            batch.projectionMatrix = oldProj
//            font.setColor(1f, 1f, 1f, 1f)
//
//            font.data.setScale(1f)
//        }

        super.postRender()

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen) {
                attemptEndFullscreen()
            } else {
                attemptFullscreen()
            }
        }
    }

    override fun dispose() {
        super.dispose()
        httpClient.close()
        BRMania.tmpMusic.emptyDirectory()
        hueBar.dispose()
    }

    fun attemptFullscreen() {
        lastWindowed = Gdx.graphics.width to Gdx.graphics.height
        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayModes.maxBy { it.width * it.height * it.refreshRate }!!)
    }

    fun attemptEndFullscreen() {
        val last = lastWindowed
        Gdx.graphics.setWindowedMode(last.first, last.second)
    }

    fun attemptResetWindow() {
        Gdx.graphics.setWindowedMode(BRMania.WIDTH, BRMania.HEIGHT)
    }

    private fun createDefaultTTFParameter(): FreeTypeFontGenerator.FreeTypeFontParameter {
        return FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            magFilter = Texture.TextureFilter.Nearest
            minFilter = Texture.TextureFilter.Linear
            genMipMaps = false
            incremental = true
            size = 24
            characters = ""
            hinting = FreeTypeFontGenerator.Hinting.AutoFull
        }
    }

    override fun createDefaultFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter())
                .setAfterLoad(fontAfterLoadFunction)
    }

    override fun createDefaultBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f
                }).setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    size *= 4
                    borderWidth *= 4
                }).setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f

                    size *= 4
                    borderWidth *= 4
                }).setAfterLoad(fontAfterLoadFunction)
    }

    private fun createCometBorderedFont(): FreeTypeFont {
        return FreeTypeFont(cometFontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 2f

                    size *= 4
                    borderWidth *= 4
                }).setAfterLoad(fontAfterLoadFunction)
    }

    private fun createKurokaneBorderedFont(): FreeTypeFont {
        return FreeTypeFont(kurokaneFontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 3f

                    size *= 4
                    borderWidth *= 4
                }).setAfterLoad(fontAfterLoadFunction)
    }
}