package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.bouncyroadmania.engine.clock.Swing
import io.github.chrislo27.bouncyroadmania.engine.clock.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.MusicCredit
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul


class MainMenuScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, MainMenuScreen>(main) {

    companion object {
        private val TMP_MATRIX = Matrix4()
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("Balloon Game") }
        private val MUSIC_BPM = 105f
        private val MUSIC_DURATION: Float = 91.428f
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

            fun endingMeasure(m: Float) {
                events += BounceEvent(0f + offset + m * 4f, 8)
                events += BounceEvent(0.5f + offset + m * 4f, 7)
                events += BounceEvent(1f + offset + m * 4f, 6)
                events += BounceEvent(1.5f + offset + m * 4f, 5)
                events += BounceEvent(2f + offset + m * 4f, 4)
                events += BounceEvent(2.6667f + offset + m * 4f, 3)
                events += BounceEvent(3f + offset + m * 4f, 2)
                events += BounceEvent(3.3333f + offset + m * 4f, 1)
            }

            // Pairs
            oneMeasureUpToCentre(0f)
            oneMeasureDownToCentre(1f)
            oneMeasureUpToCentre(2f)
            endingMeasure(3f)

            // Gliss
            glissUp(4f)
            glissDown(5f)
            glissUp(6f)
            endingMeasure(7f)

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

    override fun render(delta: Float) {
        super.render(delta)
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

        val menuPadding = 64f
        val menuTop = 420f
        val menuWidth = camera.viewportWidth - menuPadding * 2

        val titleFont = main.cometBorderedFont
        titleFont.scaleFont(camera)
        titleFont.scaleMul(0.6f)
        titleFont.drawCompressed(batch, BRMania.TITLE, menuPadding / 2, menuTop + titleFont.lineHeight, menuWidth, Align.left)
        titleFont.unscaleFont()

        val menuFont = main.kurokaneBorderedFont
        menuFont.scaleFont(camera)
        menuFont.scaleMul(0.35f)
        menuFont.drawCompressed(batch, "> ", 0f + MathHelper.getSineWave(60f / MUSIC_BPM) * 10f, menuTop + menuFont.capHeight * 0.15f, menuPadding, Align.right)
        menuFont.drawCompressed(batch, "Play", menuPadding, menuTop, menuWidth, Align.left)
        menuFont.drawCompressed(batch, "Editor", menuPadding, menuTop - menuFont.lineHeight, menuWidth, Align.left)
        menuFont.drawCompressed(batch, "Exit", menuPadding, menuTop - menuFont.lineHeight * 2f, menuWidth, Align.left)
        menuFont.unscaleFont()

        batch.end()
        batch.projectionMatrix = TMP_MATRIX
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