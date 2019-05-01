package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
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

    open inner class Event(val beat: Float, val duration: Float, val action: () -> Unit)
    inner class BounceEvent(beat: Float, val index: Int) : Event(beat, 0f, { bounce(index) })

    val clock: Clock = Clock()
    val engine = Engine(clock)
    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val gradientTop = Color(0f, 0.58f, 1f, 1f)
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
        clock.tempos.add(TempoChange(clock.tempos, 0f, 105f, Swing.STRAIGHT, 0f))

        engine.addBouncers()
        music.play()
    }

    fun doCycle() {
        events.clear()
        fun oneUnit(offset: Float) {
            fun oneMeasureUp(m: Float) {
                events += BounceEvent(0f + offset + m * 4f, 1)
                events += BounceEvent(0.5f + offset + m * 4f, 2)
                events += BounceEvent(1f + offset + m * 4f, 3)
                events += BounceEvent(1.5f + offset + m * 4f, 4)
                events += BounceEvent(2f + offset + m * 4f, 5)
                events += BounceEvent(2.5f + offset + m * 4f, 6)
                events += BounceEvent(3f + offset + m * 4f, 7)
                events += BounceEvent(3.5f + offset + m * 4f, 8)
            }

            fun oneMeasureDown(m: Float) {
                events += BounceEvent(0f + offset + m * 4f, 8)
                events += BounceEvent(0.5f + offset + m * 4f, 7)
                events += BounceEvent(1f + offset + m * 4f, 6)
                events += BounceEvent(1.5f + offset + m * 4f, 5)
                events += BounceEvent(2f + offset + m * 4f, 4)
                events += BounceEvent(2.5f + offset + m * 4f, 3)
                events += BounceEvent(3f + offset + m * 4f, 2)
                events += BounceEvent(3.5f + offset + m * 4f, 1)
            }
            oneMeasureUp(0f)
            oneMeasureDown(1f)
            oneMeasureUp(2f)
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
            endingMeasure(3f)
        }

        for (i in 0 until 10) {
            oneUnit(i * 16f)
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

        val font = main.defaultBorderedFont
        font.scaleFont(camera)

        font.scaleMul(0.5f)
        val creditWidth = 512f
        val creditPadding = 4f
        font.drawCompressed(batch, MUSIC_CREDIT, camera.viewportWidth - creditPadding - creditWidth, font.lineHeight * 3f, creditWidth, Align.right)

        font.unscaleFont()
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
        }
        events.removeIf { clock.beat >= it.beat + it.duration }
        if (clock.seconds > MUSIC_DURATION) {
            clock.seconds %= MUSIC_DURATION
            doCycle()
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