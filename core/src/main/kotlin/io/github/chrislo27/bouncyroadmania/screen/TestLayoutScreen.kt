package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.renderer.PaperProjection
import io.github.chrislo27.bouncyroadmania.renderer.PaperRenderable
import io.github.chrislo27.bouncyroadmania.renderer.PaperSprite
import io.github.chrislo27.bouncyroadmania.util.WaveUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper


class TestLayoutScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, TestLayoutScreen>(main) {

    val projector = PaperProjection(2f)
    val sprites = mutableListOf<PaperRenderable>()
    val sprite: PaperSprite

    init {
        sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_red"))

        reload()
    }

    fun reload() {
        sprites.clear()
        sprite.setPosition(500f, 400f)
        sprites += sprite

        val radius = 1200f
        for (i in 0 until 15) {
            val angle = (180 * (i / 14f)) - 90
            var sprite: PaperSprite
            if (i == 13) {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_red"))
                sprite.setOrigin(sprite.width * 0.5f, 310f)
            } else if (i == 12) {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_yellow"))
                sprite.setOrigin(sprite.width * 0.5f, 310f)
            } else {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_blue"))
            }

            val x: Float = MathUtils.cosDeg(angle) * radius
            val z: Float = (-MathUtils.sinDeg(angle) + 1) * 0.3f

            sprite.y = ((-MathUtils.sinDeg(angle) + 1) / 2f * 440f) + 180

            sprite.x = x
            sprite.posZ = z

            sprites.add(sprite)
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		sprite.posZ = WaveUtils.getBounceWave(MathHelper.getSawtoothWave(2f))

        val batch = main.batch
        batch.begin()
        batch.draw(AssetRegistry.get<Texture>("tex_gradient"), 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        projector.render(batch, sprites)
        batch.end()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            reload()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}