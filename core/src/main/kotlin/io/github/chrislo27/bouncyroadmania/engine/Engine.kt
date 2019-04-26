package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.bouncyroadmania.renderer.PaperProjection
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Engine(val clock: Clock) {

    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val entities: MutableList<Entity> = mutableListOf()
    val projector = PaperProjection(2f)
    var bouncers: List<Bouncer> = listOf()

    fun addBouncers() {
        entities.removeAll(bouncers)
        bouncers = listOf()

        val radius = 1200f
        val bouncers = mutableListOf<Bouncer>()
        for (i in -1..15) {
            val angle = (180 * (i / 14f)) - 90
            val bouncer: Bouncer = if (i == 13) {
                RedBouncer(this)
            } else if (i == 12) {
                YellowBouncer(this)
            } else {
                Bouncer(this)
            }

            val x: Float = MathUtils.cosDeg(angle) * radius
            val z: Float = (-MathUtils.sinDeg(angle) + 1) * 0.3f

            bouncer.posY = ((-MathUtils.sinDeg(angle) + 1) / 2f * 440f) + 200

            bouncer.posX = x
            bouncer.posZ = z

            entities += bouncer
            bouncers += bouncer
        }
        this.bouncers = bouncers
    }

    fun renderUpdate(delta: Float) {
        entities.forEach {
            it.renderUpdate(delta)
        }
        entities.removeIf { it.kill }
    }

    fun render(batch: SpriteBatch) {
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(AssetRegistry.get<Texture>("tex_gradient"), 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        projector.render(batch, entities)
        batch.end()
    }

}