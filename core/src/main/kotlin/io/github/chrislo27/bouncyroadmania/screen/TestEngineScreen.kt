package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.*
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.bouncyroadmania.engine.clock.Swing
import io.github.chrislo27.bouncyroadmania.engine.clock.tempo.TempoChange
import io.github.chrislo27.toolboks.ToolboksScreen


class TestEngineScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, TestEngineScreen>(main) {

    val clock: Clock = Clock()
    val engine = Engine(clock)

    var sendBallCycle = 0

    init {
        reload()
    }

    fun reload() {
        engine.entities.clear()
        clock.seconds = 0f
        clock.tempos.clear()
        clock.tempos.add(TempoChange(clock.tempos, 0f, 154f, Swing.STRAIGHT, 0f))
        sendBallCycle = 0

        val radius = 1200f
        val bouncers = mutableListOf<Bouncer>()
        for (i in -1 until 15 + 1) {
            val angle = (180 * (i / 14f)) - 90
            val bouncer: Bouncer = if (i == 13) {
                RedBouncer(engine)
            } else if (i == 12) {
                YellowBouncer(engine)
            } else {
                Bouncer(engine)
            }

            val x: Float = MathUtils.cosDeg(angle) * radius
            val z: Float = (-MathUtils.sinDeg(angle) + 1) * 0.3f

            bouncer.posY = ((-MathUtils.sinDeg(angle) + 1) / 2f * 440f) + 200

            bouncer.posX = x
            bouncer.posZ = z

            engine.entities += bouncer
            bouncers += bouncer
        }
        engine.bouncers = bouncers
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = main.batch
        engine.render(batch)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        val delta = Gdx.graphics.deltaTime
        clock.update(delta)

        val ballCycle = 1f
        if (clock.beat > ballCycle * sendBallCycle) {
            sendBallCycle++
            engine.entities += Ball(engine, if (MathUtils.randomBoolean(0.25f)) 2f else 0.5f).apply {
                val first = engine.entities.first()
                posX = first.posX
                posY = first.posY
            }
        }

        engine.renderUpdate(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reload()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            engine.entities.filterIsInstance<Bouncer>().forEach { it.bounce() }
        }
    }

    override fun getDebugString(): String? {
        return """beat: ${clock.beat}
            |seconds: ${clock.seconds}
            |bpm: ${clock.tempos.tempoAt(clock.beat)}
        """.trimMargin()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}