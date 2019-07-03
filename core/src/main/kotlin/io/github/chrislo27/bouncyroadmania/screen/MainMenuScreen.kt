package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.discord.DiscordHelper
import io.github.chrislo27.bouncyroadmania.discord.PresenceState
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.InterpolatableColor
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.input.Score
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.*
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import io.github.chrislo27.toolboks.version.Version
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class MainMenuScreen(main: BRManiaApp, playMusic: Boolean = true) : ToolboksScreen<BRManiaApp, MainMenuScreen>(main) {

    companion object {
        private val TMP_MATRIX = Matrix4()
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("Balloon Game") }
        private val MUSIC_BPM = 105f
        val TITLE = BRMania.TITLE.split(' ').map { "$it " }
        private val CYCLE_COLOURS: List<Color> by lazy {
            val tex = AssetRegistry.get<Texture>("tex_main_menu_gradient")
            val td = tex.textureData
            td.prepare()
            val shouldDispose = td.disposePixmap()
            val pixmap = td.consumePixmap()
            val ret = mutableListOf<Color>()
            for (i in 0 until 10) {
                ret += Color(pixmap.getPixel(i.coerceIn(0, pixmap.width - 1), 0))
            }
            if (shouldDispose) {
                pixmap.dispose()
            }
            ret
        }
        private val INACTIVE_TIME = 10f
        private val SPLASHES: List<String> = listOf("practiceModes", "tryOthers", "editColours", "bgImages", "skillStars", "robotMode", "bouncerCount", "difficultyRating", "gonnaMake", "gonnaPlay", "tinkTinkTink", "rhre", "setTempo")
        private val splashBag: MutableList<String> = SPLASHES.shuffled().toMutableList()
    }

    private open inner class Event(val beat: Float, val duration: Float) {
        open fun action() {
        }

        open fun onDelete() {
        }
    }

    private inner class BounceEvent(beat: Float, val pair: Int) : Event(beat, 0f) {
        override fun action() {
            bounce(pair)
        }
    }

    private inner class SingleBounceEvent(beat: Float, val index: Int) : Event(beat, 0f) {
        override fun action() {
            engine.bouncers.getOrNull(index)?.bounceAnimation()
        }
    }

    private inner class FlipXEvent(beat: Float, duration: Float) : Event(beat, duration) {
        private var started: Boolean = false
        private lateinit var positions: List<Pair<Float, Float>>
        override fun action() {
            if (!started) {
                // Record positions
                positions = engine.bouncers.map { it.posX to (engine.camera.viewportWidth - it.posX) }
                started = true
            }
            val progress = if (duration == 0f) 1f else ((engine.beat - beat) / duration).coerceIn(0f, 1f)
            engine.bouncers.forEachIndexed { i, bouncer ->
                bouncer.posX = Interpolation.circle.apply(positions[i].first, positions[i].second, progress)
            }
        }

        override fun onDelete() {
            engine.bouncers.forEachIndexed { i, bouncer ->
                bouncer.posX = positions[i].second
            }
        }
    }

    private inner class WiggleTitleEvent(beat: Float, val index: Int, val amt: Float = 1f) : Event(beat, 0f) {
        override fun action() {
            titleWiggle[index] = amt
        }
    }

    private inner class ChangeGradientEvent(beat: Float, duration: Float, val nextColor: Color) : Event(beat, duration) {
        private var started: Boolean = false
        override fun action() {
            if (!started) {
                gradientTop.beginLerp(nextColor)
                started = true
            }
            val progress = if (duration == 0f) 1f else ((engine.beat - beat) / duration).coerceIn(0f, 1f)
            gradientTop.lerp(progress)
        }

        override fun onDelete() {
            gradientTop.current.set(nextColor)
        }
    }

    open inner class MenuItem(val textKey: String, val isLocalizationKey: Boolean = true, val action: () -> Unit) {
        open val text: String get() = if (isLocalizationKey) Localization[textKey] else textKey
        var enabled: Boolean = true
    }

    data class MenuAnimation(val key: String, val speed: Float = 0.35f, var progress: Float = 0f, var reversed: Boolean = false)

    val engine: Engine = Engine().apply {
        bouncerCount = 15
    }
    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val gradientTop: InterpolatableColor get() = engine.gradientEnd
    val music: Music by lazy {
        AssetRegistry.get<Music>("music_main_menu").apply {
            isLooping = true
        }
    }
    private var lastMusicPos = 0f
    private val events: MutableList<Event> = mutableListOf()

    override val stage: Stage<MainMenuScreen> = Stage(null, main.defaultCamera, camera.viewportWidth, camera.viewportHeight)
    private val fullscreenButton: Button<MainMenuScreen> = object : Button<MainMenuScreen>(main.uiPalette, stage, stage) {
        var fullscreenState = Gdx.graphics.isFullscreen

        override fun render(screen: MainMenuScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            super.render(screen, batch, shapeRenderer)
            if (Gdx.graphics.isFullscreen != fullscreenState) {
                fullscreenState = Gdx.graphics.isFullscreen
                if (!fullscreenState) {
                    this.tooltipText = "mainMenu.tooltip.fullscreen"
                    (labels.first() as ImageLabel).image = TextureRegion(AssetRegistry.get<Texture>("ui_fullscreen"))
                } else {
                    this.tooltipText = "mainMenu.tooltip.unfullscreen"
                    (labels.first() as ImageLabel).image = TextureRegion(AssetRegistry.get<Texture>("ui_unfullscreen"))
                }
            }
        }
    }.apply {
        this.location.set(screenWidth = 0f, screenHeight = 0f,
                pixelWidth = 32f, pixelHeight = 32f, pixelX = camera.viewportWidth - 32f * 2, pixelY = camera.viewportHeight - 32f)
        this.addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = if (Gdx.graphics.isFullscreen) TextureRegion(AssetRegistry.get<Texture>("ui_unfullscreen")) else TextureRegion(AssetRegistry.get<Texture>("ui_fullscreen"))
        })
        this.leftClickAction = { _, _ ->
            if (Gdx.graphics.isFullscreen) {
                main.attemptEndFullscreen()
            } else {
                main.attemptFullscreen()
            }
        }
        this.tooltipTextIsLocalizationKey = true
        if (Gdx.graphics.isFullscreen) {
            this.tooltipText = "mainMenu.tooltip.unfullscreen"
        } else {
            this.tooltipText = "mainMenu.tooltip.fullscreen"
        }
    }
    private val versionButton: Button<MainMenuScreen>
    private var lastGhVersion: Version = Version.UNKNOWN
    private var isCursorInvisible = false

    val titleWiggle: FloatArray = FloatArray(3) { 0f }
    var hideTitle: Boolean = false
    val menuPadding = 64f
    val titleXStart = menuPadding / 2f
    val menuTop = 420f
    val menuWidth = camera.viewportWidth / 2f - menuPadding
    val menus: MutableMap<String, List<MenuItem>> = mutableMapOf()
    val menuAnimations: MutableList<MenuAnimation> = mutableListOf()
    var currentMenuKey: String = "main"
        set(value) {
            val last = field
            if (last != value) {
                field = value
                menuAnimations += MenuAnimation(last)
                menuAnimations += MenuAnimation(value, reversed = true)
            }
        }
    val currentMenu: List<MenuItem> get() = menus[currentMenuKey] ?: emptyList()
    private var clickedOnMenu: MenuItem? = null
    private var stopMusicOnHide = true
    private var inactiveTime: Float = 0f
    private var currentSplash: Pair<String, List<String>> = splashBag.let { bag ->
        val item = bag.removeAt(0)
        if (bag.isEmpty()) {
            splashBag.addAll(SPLASHES.shuffled())
        }
        item.let {
            Localization["mainMenu.splashes.$it"]
        }.let {
            it to it.split(' ').flatMap {
                val splitByHyphen = it.split('-')
                if (splitByHyphen.size >= 2) (splitByHyphen.dropLast(1).map { "$it-" } + listOf(splitByHyphen.last() + " "))
                else listOf("$it ")
            }
        }
        /*.let { it to it.toCharArray().map { it.toString() } }*/
    }

    init {
        val palette = main.uiPalette
        stage.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }
        stage.elements += object : Button<MainMenuScreen>(palette, stage, stage) {
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
        stage.elements += fullscreenButton
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenWidth = 0f, screenHeight = 0f,
                    pixelWidth = 32f, pixelHeight = 32f, pixelX = camera.viewportWidth - 32f * 3, pixelY = camera.viewportHeight - 32f)
            this.leftClickAction = { _, _ ->
                main.attemptResetWindow()
            }
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_reset_window"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.tooltip.resetWindow"
        }
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenWidth = 0f, screenHeight = 0f,
                    pixelWidth = 32f, pixelHeight = 32f, pixelX = 0f, pixelY = camera.viewportHeight - 32f)
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(BRMania.GITHUB)
            }
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_github_mark"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.github"
        }
        versionButton = Button(palette, stage, stage).apply {
            this.addLabel(TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = BRMania.VERSION.toString()
                this.textAlign = Align.left
                this.textWrapping = false
                this.fontScaleMultiplier = 0.5f
            })
            this.location.set(screenWidth = 0f, screenHeight = 0f, pixelX = 4f, pixelWidth = 256f, pixelHeight = 24f)
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.checkForUpdates"
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("https://github.com/chrislo27/BouncyRoadMania/releases/latest")
            }
        }
        stage.elements += versionButton
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
        stage.elements += TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), stage, stage).apply {
            this.location.set(screenX = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = -450f - 4f, pixelWidth = 450f, pixelHeight = 16f * 4 + 32 * 3)
            this.isLocalizationKey = false
            this.text = "This game is a work-in-progress.\nAll features are subject to change."
            this.textAlign = Align.right
            this.textWrapping = false
            this.fontScaleMultiplier = 0.75f
            this.visible = false
        }

        stage.updatePositions()
    }

    init {
        val mainDebugItem = MenuItem("Debug", isLocalizationKey = false) {
            currentMenuKey = "test"
        }
        menus["main"] = listOf(
                MenuItem("mainMenu.play") {
                    main.screen = TransitionScreen(main, main.screen, GameSelectScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                },
                MenuItem("mainMenu.practice") {
                    currentMenuKey = "practices"
                },
                MenuItem("mainMenu.edit") {
                    val editorFactory = { EditorScreen(main) }
                    main.screen = TransitionScreen(main, main.screen, EditorLoadingScreen(main, editorFactory), WipeTo(Color.BLACK, 0.35f), null)
                },
//                MenuItem("mainMenu.settings") {
//                    currentMenuKey = "settings"
//                }.apply {
//                    this.enabled = false
//                },
                mainDebugItem,
                MenuItem("mainMenu.quit") {
                    Gdx.app.exit()
                    thread(isDaemon = true) {
                        Thread.sleep(500L)
                        exitProcess(0)
                    }
                }
        ) - (if (!Toolboks.debugMode) listOf(mainDebugItem) else listOf())
        menus["settings"] = listOf(
                MenuItem("mainMenu.settings.video") {
                }.apply {
                    this.enabled = false
                },
                MenuItem("mainMenu.settings.audio") {
                }.apply {
                    this.enabled = false
                },
                MenuItem("mainMenu.settings.controls") {
                }.apply {
                    this.enabled = false
                },
                MenuItem("mainMenu.back") {
                    currentMenuKey = "main"
                }
        )
        menus["practices"] = listOf(
                MenuItem((if (!main.preferences.getBoolean(PreferenceKeys.PRACTICE_COMPLETE_PREFIX + PracticeStage.STANDARD.name, false)) "${Localization["mainMenu.practice.new"]} " else "") + Localization["mainMenu.practice.standard"], isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, LoadingPracticeScreen(main, PracticeStage.STANDARD), WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem((if (!main.preferences.getBoolean(PreferenceKeys.PRACTICE_COMPLETE_PREFIX + PracticeStage.LONG_SHORT_FAST.name, false)) "${Localization["mainMenu.practice.new"]} " else "") + Localization["mainMenu.practice.longShortFast"], isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, LoadingPracticeScreen(main, PracticeStage.LONG_SHORT_FAST), WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("mainMenu.back") {
                    currentMenuKey = "main"
                }
        )

        // Test menus
        menus["test"] = listOf(
                MenuItem("TestEngineScreen", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, TestEngineScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                },
                MenuItem("ResultsScreen", isLocalizationKey = false) {
                    currentMenuKey = "test_results"
                },
                MenuItem("Intro", isLocalizationKey = false) {
                    this.stopMusicOnHide = false
                    music.stop()
                    main.screen = AssetRegistryLoadingScreen(main)
                },
                MenuItem("TinyFD", isLocalizationKey = false) {
                    currentMenuKey = "test_tinyfd"
                },
                MenuItem("Screen wipe (-)", isLocalizationKey = false) {
                    stopMusicOnHide = false
                    main.screen = TransitionScreen(main, main.screen, main.screen, WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                },
                MenuItem("Screen wipe (+)", isLocalizationKey = false) {
                    stopMusicOnHide = false
                    main.screen = TransitionScreen(main, main.screen, main.screen, WipeTo(Color.BLACK, 0.35f, slope = 4f), WipeFrom(Color.BLACK, 0.35f, slope = 4f))
                },
                MenuItem("mainMenu.back") {
                    currentMenuKey = "main"
                }
        )
        menus["test_tinyfd"] = listOf(
                MenuItem("File open", isLocalizationKey = false) {
                    println(TinyFDWrapper.openFile("Open a file", "", null))
                },
                MenuItem("File save", isLocalizationKey = false) {
                    println(TinyFDWrapper.saveFile("Save to a file", "", null))
                },
                MenuItem("File save (images)", isLocalizationKey = false) {
                    println(TinyFDWrapper.saveFile("Save to an image file", "", TinyFDWrapper.Filter(listOf("*.jpg", "*.png"), "Image files (*.jpg, *.png)")))
                },
                MenuItem("Folder select", isLocalizationKey = false) {
                    println(TinyFDWrapper.selectFolder("Select a folder", ""))
                },
                MenuItem("Colour select", isLocalizationKey = false) {
                    println(TinyFDWrapper.selectColor("Select a colour", Color.PINK))
                },
                MenuItem("mainMenu.back") {
                    currentMenuKey = "test"
                }
        )
        menus["test_results"] = listOf(
                MenuItem("Score: 20", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(20, 20f, false, false, Localization["results.default.title"], Localization["results.default.second.negative"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("Score: 55", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(55, 55f, false, false, Localization["results.default.title"], Localization["results.default.first.negative"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("Score: 65", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(65, 65f, false, false, Localization["results.default.title"], Localization["results.default.ok"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("Score: 75", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(75, 75f, false, false, Localization["results.default.title"], Localization["results.default.first.positive"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("Score: 80", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(80, 80f, false, false, Localization["results.default.title"], Localization["results.default.first.positive"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("Score: 100", isLocalizationKey = false) {
                    main.screen = TransitionScreen(main, main.screen, ResultsScreen(main,
                            Score(100, 100f, false, true, Localization["results.default.title"], Localization["results.default.second.positive"], "line2\nsecond line2")),
                            WipeTo(Color.BLACK, 0.35f), null)
                },
                MenuItem("mainMenu.back") {
                    currentMenuKey = "test"
                }
        )
    }

    init {
        engine.playState = PlayState.STOPPED
        reload(playMusic)
        doCycle()
        Gdx.app.postRunnable {
            Gdx.app.postRunnable {
                engine.playState = PlayState.PLAYING
            }
        }
    }

    fun reload(doPlayMusic: Boolean = true) {
        engine.entities.clear()
        engine.requiresPlayerInput = false
        engine.seconds = 0f
        engine.tempos.clear()
        engine.tempos.add(TempoChange(engine.tempos, 0f, MUSIC_BPM, Swing.STRAIGHT, 0f))

        engine.addBouncers()
        music.volume = if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) 0f else 1f
        if (doPlayMusic) {
            music.play()
        }
    }

    fun doCycle() {
        events.forEach {
            it.action()
            it.onDelete()
        }
        events.clear()
        fun oneUnit(offset: Float) {
            fun oneMeasureUpToCentre(m: Float) {
                events += BounceEvent(0f + offset + m * 4f, 1)
                events += BounceEvent(0.5f + offset + m * 4f, 2)
                events += BounceEvent(1f + offset + m * 4f, 3)
                events += BounceEvent(1.5f + offset + m * 4f, 4)
                events += BounceEvent(2f + offset + m * 4f, 5)
                events += BounceEvent(2.25f + offset + m * 4f, 6)
                events += BounceEvent(2.5f + offset + m * 4f, 7)
                events += BounceEvent(2.75f + offset + m * 4f, 8)
            }

            fun oneMeasureDownToCentre(m: Float) {
                events += BounceEvent(0f + offset + m * 4f, 8)
                events += BounceEvent(0.5f + offset + m * 4f, 7)
                events += BounceEvent(1f + offset + m * 4f, 6)
                events += BounceEvent(1.5f + offset + m * 4f, 5)
                events += BounceEvent(2f + offset + m * 4f, 4)
                events += BounceEvent(2.25f + offset + m * 4f, 3)
                events += BounceEvent(2.5f + offset + m * 4f, 2)
                events += BounceEvent(2.75f + offset + m * 4f, 1)
            }

            fun glissUp(m: Float) {
                for (i in 0..14) {
                    events += SingleBounceEvent((i * 0.25f) + offset + m * 4f, 14 - i + 1)
                }
            }

            fun glissDown(m: Float) {
                for (i in 0..14) {
                    events += SingleBounceEvent((i * 0.25f) + offset + m * 4f, i + 1)
                }
            }

            fun endingMeasure(m: Float, wiggle: Boolean) {
                events += BounceEvent(0f + offset + m * 4f, 8)
                events += BounceEvent(0.5f + offset + m * 4f, 7)
                events += BounceEvent(1f + offset + m * 4f, 6)
                events += BounceEvent(1.5f + offset + m * 4f, 5)
                events += BounceEvent(2f + offset + m * 4f, 4)
                events += BounceEvent(2.6667f + offset + m * 4f, 3)
                events += BounceEvent(3f + offset + m * 4f, 2)
                events += BounceEvent(3.3333f + offset + m * 4f, 1)
                if (wiggle) {
                    events += WiggleTitleEvent(2.6667f + offset + m * 4f, 2)
                    events += WiggleTitleEvent(3f + offset + m * 4f, 1)
                    events += WiggleTitleEvent(3.3333f + offset + m * 4f, 0)
                }
            }

            // Pairs
            oneMeasureUpToCentre(0f)
            oneMeasureDownToCentre(1f)
            oneMeasureUpToCentre(2f)
            endingMeasure(3f, false)

            // Gliss
            glissUp(4f)
            glissDown(5f)
            glissUp(6f)
            endingMeasure(7f, true)

//            events += FlipXEvent(8f * 4f + offset - 0.25f, 0.25f)
        }

        for (i in 0 until 5) {
            oneUnit(i * 32f)
        }
        for (i in 1..10) {
            // Gradient
            val nextColor = CYCLE_COLOURS[i - 1].cpy()
            events += ChangeGradientEvent(i * 16f - 0.25f, 0.25f, nextColor)
        }
    }

    private fun bounce(index: Int) {
        engine.bouncers.getOrNull(index)?.bounceAnimation()
        engine.bouncers.getOrNull(14 - (index - 2))?.bounceAnimation()
    }

    fun getMenuIndex(): Int {
        return if (camera.getInputX() > menuWidth) -1 else {
            Math.floor((camera.getInputY() - (menuTop + 10f)) / -60.5).toInt().coerceAtLeast(-1)
        }
    }

    override fun render(delta: Float) {
        // Engine and events updates
        if (music.isPlaying) {
            engine.update(Gdx.graphics.deltaTime)

            val beat = engine.beat
            events.forEach {
                if (beat >= it.beat) {
                    it.action()
                }
                if (beat >= it.beat + it.duration) {
                    it.onDelete()
                }
            }
            events.removeIf { beat >= it.beat + it.duration }
            if (lastMusicPos > music.position + 1f) {
                engine.seconds = music.position
                doCycle()
            } else if (!MathUtils.isEqual(engine.seconds, music.position, 0.1f) && music.position > 0.1f) {
                engine.seconds = music.position
            }
            lastMusicPos = music.position
        }

        for (i in 0 until titleWiggle.size) {
            if (titleWiggle[i] != 0f) {
                val sign = Math.signum(titleWiggle[i])
                titleWiggle[i] -= sign * Gdx.graphics.deltaTime * 8f
                if (Math.signum(titleWiggle[i]) != sign && titleWiggle[i] != 0f) {
                    titleWiggle[i] = 0f
                }
            }
        }
        menuAnimations.forEach { it.progress += Gdx.graphics.deltaTime / it.speed }
        menuAnimations.removeIf { it.progress >= 1f }

        val batch = main.batch

        // Engine rendering
        engine.render(batch)

        // UI rendering
        if (inactiveTime < INACTIVE_TIME) {
            TMP_MATRIX.set(batch.projectionMatrix)
            camera.update()
            batch.projectionMatrix = camera.combined
            batch.begin()

            if (!hideTitle) {
                val titleFont = main.cometBorderedFont
                titleFont.scaleFont(camera)
                titleFont.scaleMul(0.6f)
                var titleX = titleXStart
                TITLE.forEachIndexed { i, s ->
                    titleX += titleFont.draw(batch, s, titleX, menuTop + titleFont.lineHeight + titleFont.capHeight * titleWiggle[i]).width
                }
                titleFont.unscaleFont()
            }

            val menuIndex = getMenuIndex()
            val currentMenu = currentMenu
            val menuFont = main.kurokaneBorderedFont
            menuFont.scaleFont(camera)
            menuFont.scaleMul(0.35f)
            menuAnimations.forEach { animation ->
                renderMenu(batch, menus.getValue(animation.key), menuFont,
                        Interpolation.smooth.apply(0f, -menuPadding, animation.progress) + (if (animation.reversed) menuPadding else 0f),
                        0f, Interpolation.smooth.apply(1f, 0f, if (!animation.reversed) animation.progress else (1f - animation.progress)))
            }
            if (menuAnimations.none { it.reversed && it.key == currentMenuKey }) {
                renderMenu(batch, currentMenu, menuFont, 0f, 0f, 1f)
            }

            val c = clickedOnMenu
            if (menuIndex in 0 until currentMenu.size && (c == null || c === currentMenu[menuIndex])) {
                if (!currentMenu[menuIndex].enabled) {
                    menuFont.setColor(0.5f, 0.5f, 0.5f, 0f)
                } else if (c != null && c === currentMenu[menuIndex]) {
                    menuFont.setColor(0.5f, 1f, 1f, 1f)
                }
                menuFont.drawCompressed(batch, "> ", 0f + MathHelper.getSineWave(60f / MUSIC_BPM) * 10f, menuTop + menuFont.capHeight * 0.15f - menuFont.lineHeight * menuIndex, menuPadding, Align.right)
                menuFont.setColor(1f, 1f, 1f, 1f)
            }
            menuFont.unscaleFont()

            val tip = currentSplash
            if (tip.first.isNotEmpty()) {
                val font = main.defaultBorderedFont
                font.scaleFont(camera)
                val availableWidth = camera.viewportWidth * 0.9f
                val unboundedWidth = font.getTextWidth(tip.first)
                val width = unboundedWidth.coerceAtMost(availableWidth)
                var posX = camera.viewportWidth / 2f - width / 2f
                val baseY = camera.viewportHeight * 0.9f
                if (unboundedWidth > availableWidth) {
                    font.data.scaleX *= (width / unboundedWidth)
                }
                tip.second.forEachIndexed { i, word ->
                    val beatPulse = Interpolation.smoother.apply(((1f - engine.beat % 1) - 0.6f).coerceIn(0f, 0.4f) / 0.4f)
                    val parity = if (i % 2 == 0) 1 else -1
                    val beatParity = if (engine.beat.toInt() % 2 == 0) 1 else -1
                    val offsetY = (parity * beatParity * beatPulse) * 4f
//                    val offsetY = MathUtils.sinDeg(360f * MathHelper.getSawtoothWave(System.currentTimeMillis() - i * 30L, 0.5f * 60f / engine.tempos.tempoAtSeconds(engine.seconds))) * 1f
                    posX += font.draw(batch, word, posX, baseY + offsetY, 0f, Align.left, false).width
                }

                font.unscaleFont()
            }

            batch.end()
            batch.projectionMatrix = TMP_MATRIX
        }
        stage.visible = inactiveTime < INACTIVE_TIME

        super.render(delta)
    }

    fun renderMenu(batch: SpriteBatch, menu: List<MenuItem>, menuFont: BitmapFont, xOffset: Float, yOffset: Float, alpha: Float) {
        menu.forEachIndexed { index, menuItem ->
            menuFont.setColor(1f, 1f, 1f, alpha)
            if (!menuItem.enabled) {
                menuFont.setColor(0.5f, 0.5f, 0.5f, alpha)
            } else if (menuItem === clickedOnMenu) {
                menuFont.setColor(0.5f, 1f, 1f, alpha)
            }
            menuFont.drawCompressed(batch, menuItem.text, menuPadding + xOffset, menuTop - menuFont.lineHeight * index + yOffset, menuWidth, Align.left)
            menuFont.setColor(1f, 1f, 1f, 1f)
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        val menuIndex = getMenuIndex()
        val currentMenu = currentMenu
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            clickedOnMenu = currentMenu.getOrNull(menuIndex)
        }
        if (Gdx.input.isButtonJustReleased(Input.Buttons.LEFT)) {
            val c = clickedOnMenu
            if (c != null && currentMenu.getOrNull(menuIndex) === c && c.enabled) {
                c.action.invoke()
            }
            clickedOnMenu = null
        }
        val ghv = main.githubVersion
        if (ghv != lastGhVersion) {
            if (!ghv.isUnknown && ghv > BRMania.VERSION) {
                versionButton.tooltipText = "mainMenu.checkForUpdates.new"
                (versionButton.labels.first() as TextLabel).textColor = if (main.preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0) >= 3) Color.RED else Color.ORANGE
            }
        }
        if (menuIndex !in 0 until currentMenu.size) {
            inactiveTime += Gdx.graphics.deltaTime
        }


        if (inactiveTime >= INACTIVE_TIME && main.screen == this) {
            if (!isCursorInvisible) {
                isCursorInvisible = true
                Gdx.graphics.setCursor(AssetRegistry["cursor_invisible"])
            }
        } else {
            if (isCursorInvisible) {
                isCursorInvisible = false
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        if (!MathUtils.isEqual(engine.seconds, music.position, 0.1f)) {
            engine.seconds = music.position
        }
    }

    override fun getDebugString(): String? {
        return """beat: ${engine.beat}
            |seconds: ${engine.seconds}
            |bpm: ${engine.tempos.tempoAt(engine.beat)}
            |events: ${events.size}
            |music: ${music.position}
            |  offset: ${engine.seconds - music.position}
            |inactiveTime: $inactiveTime
        """.trimMargin()
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        inactiveTime = 0f
        return super.mouseMoved(screenX, screenY)
    }

    override fun scrolled(amount: Int): Boolean {
        inactiveTime = 0f
        return super.scrolled(amount)
    }

    override fun keyDown(keycode: Int): Boolean {
        inactiveTime = 0f
        return super.keyDown(keycode)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        inactiveTime = 0f
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun show() {
        super.show()
        DiscordHelper.updatePresence(PresenceState.MainMenu)
        hideTitle = false
    }

    override fun hide() {
        super.hide()
        if (stopMusicOnHide) {
            music.fadeTo(0f, 0.25f)
        }
        stopMusicOnHide = true
        isCursorInvisible = false
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
