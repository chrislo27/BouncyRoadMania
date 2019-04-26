package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Ball
import io.github.chrislo27.bouncyroadmania.engine.Bouncer
import io.github.chrislo27.bouncyroadmania.engine.Engine
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

        engine.addBouncers()
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