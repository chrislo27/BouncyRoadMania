package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.*
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
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
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class MainMenuScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, MainMenuScreen>(main) {

    companion object {
        private val TMP_MATRIX = Matrix4()
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("Balloon Game") }
        private val MUSIC_BPM = 105f
        private val MUSIC_DURATION: Float = 91.428f
        private val TITLE = BRMania.TITLE.split(' ').map { "$it " }
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
        private lateinit var oldColor: Color
        override fun action() {
            if (!started) {
                oldColor = gradientTop.cpy()
                started = true
            }
            gradientTop.set(oldColor)
            val progress = if (duration == 0f) 1f else ((engine.beat - beat) / duration).coerceIn(0f, 1f)
            gradientTop.lerp(nextColor, progress)
        }

        override fun onDelete() {
            gradientTop.set(nextColor)
        }
    }

    open inner class MenuItem(val textKey: String, val isLocalizationKey: Boolean = true, val action: () -> Unit) {
        open val text: String get() = if (isLocalizationKey) Localization[textKey] else textKey
        var enabled: Boolean = true
    }

    data class MenuAnimation(val key: String, val speed: Float = 0.35f, var progress: Float = 0f, var reversed: Boolean = false)

    val engine: Engine = Engine()
    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val gradientStart: Color = Color().set(CYCLE_COLOURS.last())
    val gradientTop: Color = gradientStart.cpy()
    val gradientBottom = Color(0f, 0f, 0f, 1f)
    val music: Music by lazy {
        AssetRegistry.get<Music>("music_main_menu").apply {
            isLooping = true
        }
    }
    private var lastMusicPos = 0f
    private val events: MutableList<Event> = mutableListOf()
    override val stage: Stage<MainMenuScreen> = Stage(null, camera, camera.viewportWidth, camera.viewportHeight)

    val titleWiggle: FloatArray = FloatArray(3) { 0f }
    val menuPadding = 64f
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

    init {
        stage.tooltipElement = TextLabel(main.uiPalette, stage, stage).apply {
            this.textAlign = Align.center
            this.fontScaleMultiplier = 0.75f
            this.background = true
        }
        stage.elements += object : Button<MainMenuScreen>(main.uiPalette, stage, stage) {
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
                this.tooltipText = "mainMenu.tooltip.${if (!old) "mute" else "unmute"}Music"
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.tooltip.${if (muted) "mute" else "unmute"}Music"
        }
        stage.elements += Button(main.uiPalette, stage, stage).apply {
            this.location.set(screenWidth = 0f, screenHeight = 0f,
                    pixelWidth = 32f, pixelHeight = 32f, pixelX = camera.viewportWidth - 32f * 2, pixelY = camera.viewportHeight - 32f)
            this.leftClickAction = { _, _ ->
                main.attemptFullscreen()
            }
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_fullscreen"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.tooltip.fullscreen"
        }
        stage.elements += Button(main.uiPalette, stage, stage).apply {
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
    }

    init {
        menus["main"] = listOf(
                MenuItem("mainMenu.play") {
                    main.screen = TransitionScreen(main, main.screen, TestEngineScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                },
                MenuItem("mainMenu.edit") {
                    val editor = EditorScreen(main) // ScreenRegistry.getNonNull("editor")
                    main.screen = TransitionScreen(main, main.screen, editor, WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                },
                MenuItem("mainMenu.settings") {
                    currentMenuKey = "settings"
                },
                MenuItem("Debug", isLocalizationKey = false) {
                    currentMenuKey = "test"
                },
                MenuItem("mainMenu.quit") {
                    Gdx.app.exit()
                    thread(isDaemon = true) {
                        Thread.sleep(500L)
                        exitProcess(0)
                    }
                }
        )
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

        // Test menus
        menus["test"] = listOf(
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
                    println(TinyFDWrapper.openFile("Open a file", "", false, null))
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
    }

    init {
        engine.playState = PlayState.STOPPED
        reload()
        doCycle()
        Gdx.app.postRunnable {
            Gdx.app.postRunnable {
                engine.playState = PlayState.PLAYING
            }
        }
    }

    fun reload() {
        engine.entities.clear()
        engine.seconds = 0f
        engine.tempos.clear()
        engine.tempos.add(TempoChange(engine.tempos, 0f, MUSIC_BPM, Swing.STRAIGHT, 0f))

        engine.addBouncers()
        music.play()
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
        val batch = main.batch

        // Background
        TMP_MATRIX.set(batch.projectionMatrix)
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.drawQuad(0f, 0f, gradientBottom, camera.viewportWidth, 0f, gradientBottom, camera.viewportWidth, camera.viewportHeight, gradientTop, 0f, camera.viewportHeight, gradientTop)
        batch.end()
        batch.projectionMatrix = TMP_MATRIX

        // Engine rendering
        engine.render(batch)

        // UI rendering
        TMP_MATRIX.set(batch.projectionMatrix)
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()

        val borderedFont = main.defaultBorderedFont
        borderedFont.scaleFont(camera)
        borderedFont.scaleMul(0.5f)
        val creditPadding = 4f
        val creditWidth = camera.viewportWidth - creditPadding * 4f
        val creditHeight = borderedFont.lineHeight * 3f
        borderedFont.drawCompressed(batch, MUSIC_CREDIT, camera.viewportWidth - creditPadding - creditWidth, creditHeight, creditWidth, Align.right)
        borderedFont.drawCompressed(batch, BRMania.VERSION.toString(), creditPadding, borderedFont.lineHeight, creditWidth, Align.left)
        borderedFont.unscaleFont()

        val titleFont = main.cometBorderedFont
        titleFont.scaleFont(camera)
        titleFont.scaleMul(0.6f)
        var titleX = menuPadding / 2f
        TITLE.forEachIndexed { i, s ->
            titleX += titleFont.draw(batch, s, titleX, menuTop + titleFont.lineHeight + titleFont.capHeight * titleWiggle[i]).width
        }
//        titleFont.drawCompressed(batch, BRMania.TITLE, menuPadding / 2, menuTop + titleFont.lineHeight, camera.viewportWidth - menuPadding * 2, Align.left)
        titleFont.unscaleFont()

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

        batch.end()
        batch.projectionMatrix = TMP_MATRIX

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
        engine.update(Gdx.graphics.deltaTime)

        events.forEach {
            if (engine.beat >= it.beat) {
                it.action()
            }
            if (engine.beat >= it.beat + it.duration) {
                it.onDelete()
            }
        }
        events.removeIf { engine.beat >= it.beat + it.duration }
        if (lastMusicPos > music.position) {
            engine.seconds = music.position
            doCycle()
        } else if (!MathUtils.isEqual(engine.seconds, music.position, 0.1f) && music.position > 0.1f) {
            engine.seconds = music.position
        }
        lastMusicPos = music.position

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
        """.trimMargin()
    }

    override fun hide() {
        super.hide()
        if (stopMusicOnHide) {
            music.stop()
        }
        stopMusicOnHide = true
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
