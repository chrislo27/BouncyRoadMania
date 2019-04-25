package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Bouncer
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.RedBouncer
import io.github.chrislo27.bouncyroadmania.engine.YellowBouncer
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.toolboks.ToolboksScreen


class TestEngineScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, TestEngineScreen>(main) {

    val clock: Clock = Clock()
    val engine = Engine(clock)

    init {
        reload()
    }

    fun reload() {
        engine.entities.clear()

        val radius = 1200f
        for (i in 0 until 15) {
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
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        engine.renderUpdate(Gdx.graphics.deltaTime)

        val batch = main.batch
        engine.render(batch)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reload()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            engine.entities.filterIsInstance<Bouncer>().forEach { it.bounce() }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}