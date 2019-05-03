package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.bouncyroadmania.engine.clock.Swing
import io.github.chrislo27.bouncyroadmania.engine.clock.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.MusicCredit
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
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
    }

    open inner class Event(val beat: Float, val duration: Float) {
        open fun action() {
        }
        open fun onDelete() {
        }
    }

    inner class BounceEvent(beat: Float, val pair: Int) : Event(beat, 0f) {
        override fun action() {
            bounce(pair)
        }
    }

    inner class SingleBounceEvent(beat: Float, val index: Int) : Event(beat, 0f) {
        override fun action() {
            engine.bouncers.getOrNull(index)?.bounceAnimation()
        }
    }

    inner class FlipXEvent(beat: Float, duration: Float) : Event(beat, duration) {
        private var started: Boolean = false
        private lateinit var positions: List<Pair<Float, Float>>
        override fun action() {
            if (!started) {
                // Record positions
                positions = engine.bouncers.map { it.posX to (engine.camera.viewportWidth - it.posX) }
                started = true
            }
            val progress = if (duration == 0f) 1f else ((clock.beat - beat) / duration).coerceIn(0f, 1f)
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

    inner class WiggleTitleEvent(beat: Float, val index: Int, val amt: Float = 1f) : Event(beat, 0f) {
        override fun action() {
            titleWiggle[index] = amt
        }
    }

    inner class ChangeGradientEvent(beat: Float, duration: Float, val nextColor: Color) : Event(beat, duration) {
        private var started: Boolean = false
        private lateinit var oldColor: Color
        override fun action() {
            if (!started) {
                oldColor = gradientTop.cpy()
                started = true
            }
            gradientTop.set(oldColor)
            val progress = if (duration == 0f) 1f else ((clock.beat - beat) / duration).coerceIn(0f, 1f)
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

    val clock: Clock = Clock()
    val engine = Engine(clock)
    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val gradientStart = Color(0f, 0.58f, 1f, 1f)
    val gradientTop: Color = gradientStart.cpy()
    val gradientBottom = Color(0f, 0f, 0f, 1f)
    val music: Music by lazy {
        AssetRegistry.get<Music>("music_main_menu").apply {
            isLooping = true
        }
    }
    private val events: MutableList<Event> = mutableListOf()
    override val stage: Stage<MainMenuScreen> = Stage(null, camera, camera.viewportWidth, camera.viewportHeight)

    val titleWiggle: FloatArray = FloatArray(3) { 0f }
    val menuPadding = 64f
    val menuTop = 420f
    val menuWidth = camera.viewportWidth / 2f - menuPadding
    val menus: MutableMap<String, List<MenuItem>> = mutableMapOf()
    var currentMenuKey: String = "main"
    val currentMenu: List<MenuItem> get() = menus[currentMenuKey] ?: emptyList()
    private var clickedOnMenu: MenuItem? = null

    init {
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
            music.volume = if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) 0f else 1f
            label.image = if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) muted else unmuted
            this.leftClickAction = { _, _ ->
                val old = main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)
                main.preferences.putBoolean(PreferenceKeys.MUTE_MUSIC, !old).flush()
                music.volume = if (!old) 0f else 1f
                label.image = if (!old) muted else unmuted
            }
        }
    }

    init {
        menus["main"] = listOf(
                MenuItem("mainMenu.play") {

                },
                MenuItem("mainMenu.edit") {

                },
                MenuItem("mainMenu.settings") {

                }.apply {
                    this.enabled = false
                },
                MenuItem("mainMenu.quit") {
                    Gdx.app.exit()
                    thread(isDaemon = true) {
                        Thread.sleep(500L)
                        exitProcess(0)
                    }
                }
        )
    }

    init {
        clock.paused = true
        reload()
        doCycle()
        Gdx.app.postRunnable {
            Gdx.app.postRunnable {
                clock.paused = false
            }
        }
    }

    fun reload() {
        engine.entities.clear()
        clock.seconds = 0f
        clock.tempos.clear()
        clock.tempos.add(TempoChange(clock.tempos, 0f, MUSIC_BPM, Swing.STRAIGHT, 0f))

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

        val hsv = FloatArray(3)
        for (i in 0 until 5) {
            oneUnit(i * 32f)
        }
        for (i in 1..10) {
            // Gradient
            gradientStart.toHsv(hsv)
            val nextColor = Color(1f, 1f, 1f, 1f).fromHsv((hsv[0] + i * 360f / 10) % 360f, hsv[1], hsv[2])
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
        currentMenu.forEachIndexed { index, menuItem ->
            menuFont.setColor(1f, 1f, 1f, 1f)
            if (!menuItem.enabled) {
                menuFont.setColor(0.5f, 0.5f, 0.5f, 1f)
            } else if (menuItem === clickedOnMenu) {
                menuFont.setColor(0.5f, 1f, 1f, 1f)
            }
            menuFont.drawCompressed(batch, menuItem.text, menuPadding, menuTop - menuFont.lineHeight * index, menuWidth, Align.left)
            menuFont.setColor(1f, 1f, 1f, 1f)
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

    override fun renderUpdate() {
        super.renderUpdate()
        clock.update(Gdx.graphics.deltaTime)
        engine.renderUpdate(Gdx.graphics.deltaTime)

        events.forEach {
            if (clock.beat >= it.beat) {
                it.action()
            }
            if (clock.beat >= it.beat + it.duration) {
                it.onDelete()
            }
        }
        events.removeIf { clock.beat >= it.beat + it.duration }
        if (clock.seconds > MUSIC_DURATION) {
            clock.seconds %= MUSIC_DURATION
            doCycle()
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

        if (!MathUtils.isEqual(clock.seconds, music.position, 0.1f)) {
            clock.seconds = music.position
        }
    }

    override fun getDebugString(): String? {
        return """beat: ${clock.beat}
            |seconds: ${clock.seconds}
            |bpm: ${clock.tempos.tempoAt(clock.beat)}
            |events: ${events.size}
        """.trimMargin()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
