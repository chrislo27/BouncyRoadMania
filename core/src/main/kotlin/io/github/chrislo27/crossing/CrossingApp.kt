package io.github.chrislo27.crossing

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ResizeAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.setHSB
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl
import java.io.File


class CrossingApp(logger: Logger, logToFile: File?)
    : ToolboksGame(logger, logToFile, Crossing.VERSION, Crossing.WIDTH to Crossing.HEIGHT, ResizeAction.KEEP_ASPECT_RATIO, Crossing.WIDTH to Crossing.HEIGHT) {

    companion object {
        lateinit var instance: CrossingApp
            private set

        val httpClient: AsyncHttpClient = Dsl.asyncHttpClient(DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).setCompressionEnforced(true))

        private const val RAINBOW_STR = "RAINBOW"
        private val rainbowColor: Color = Color()

        init {
            Colors.put("X", Color.CLEAR)
        }
    }

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin_merged.ttf") }
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

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"

    val defaultFontFTF: FreeTypeFont
        get() = fonts[defaultFontKey]
    val defaultBorderedFontFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontKey]
    val defaultFontLargeFTF: FreeTypeFont
        get() = fonts[defaultFontLargeKey]
    val defaultBorderedFontLargeFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontLargeKey]

    lateinit var preferences: Preferences
        private set
    var versionTextWidth: Float = -1f
        private set

    override fun getTitle(): String = "${Crossing.TITLE} ${Crossing.VERSION}"

    override val programLaunchArguments: List<String>
        get() = Crossing.launchArguments

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("${Crossing.TITLE} $versionString is starting...")
        // 1.8.0_144
        // 9.X.Y(extra)
        val javaVersion = System.getProperty("java.version").trim()
        Toolboks.LOGGER.info("Running on JRE $javaVersion")

        instance = this

        // localization stuff
        run {
            Localization.loadBundlesFromLangFile()
            Localization.logMissingLocalizations()
        }

        // font stuff
        run {
            fonts[defaultFontLargeKey] = createDefaultLargeFont()
            fonts[defaultBorderedFontLargeKey] = createDefaultLargeBorderedFont()
            fonts.loadUnloaded(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
        }

        // preferences
        preferences = Gdx.app.getPreferences("Crossing")
    }

    override fun preRender() {
        rainbowColor.setHSB(MathHelper.getSawtoothWave(2f), 0.8f, 0.8f)
        Colors.put(RAINBOW_STR, rainbowColor)
        super.preRender()
    }

    override fun postRender() {
        val screen = screen
        if (screen !is HidesVersionText || !screen.hidesVersionText) {
            val font = defaultBorderedFont
            font.data.setScale(0.5f)

            font.setColor(1f, 1f, 1f, 1f)

            val oldProj = batch.projectionMatrix
            batch.projectionMatrix = defaultCamera.combined
            batch.begin()
            val layout = font.draw(batch, Crossing.VERSION.toString(),
                                   0f,
                                   (font.capHeight) + (2f / Crossing.HEIGHT) * defaultCamera.viewportHeight,
                                   defaultCamera.viewportWidth, Align.right, false)
            versionTextWidth = layout.width
            batch.end()
            batch.projectionMatrix = oldProj
            font.setColor(1f, 1f, 1f, 1f)

            font.data.setScale(1f)
        }

        super.postRender()
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
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f

                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }
}