package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.renderer.PaperProjection
import io.github.chrislo27.bouncyroadmania.renderer.PaperSprite
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry


class TestScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, TestScreen>(main) {

    val projector = PaperProjection(2f)
    val sprite: PaperSprite

    init {
        sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_red"))
        sprite.setPosition(400f, 300f)

//		projector.sprites.add(sprite)
//
        var sprite: PaperSprite
//				sprite = PaperSprite(AssetRegistry.getTexture("tex_bouncer_red"))
//		sprite.setPosition(416f, 300f)
//		sprite.z = 0.6f
//		projector.sprites.add(sprite)
//
//		sprite = PaperSprite(AssetRegistry.getTexture("tex_bouncer_red"))
//		sprite.setPosition(700f, 300f)
//		sprite.z = 0f
//		projector.sprites.add(sprite)

        val radius = 1200f
        for (i in 0..15) {
            val angle = (180 * (i / 15f)) - 90

            if (i == 14) {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_red"))
                sprite.setOrigin(sprite.width * 0.5f, 310f)
            } else if (i == 13) {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_yellow"))
                sprite.setOrigin(sprite.width * 0.5f, 310f)
            } else {
                sprite = PaperSprite(AssetRegistry.get<Texture>("tex_bouncer_blue"))
            }

            val x: Float = MathUtils.cosDeg(angle) * radius
            val z: Float = (((MathUtils.sinDeg(angle) * radius) / -1200f) + 1) / 2f

            sprite.y = (z * 440f) + 180

            sprite.x = x
            sprite.z = z

            projector.sprites.add(sprite)
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

//		sprite.z = WaveUtils.getBounceWave(MathHelper.getSawtoothWave(1.25f))

        val batch = main.batch
        batch.begin()
        batch.draw(AssetRegistry.get<Texture>("tex_gradient"), 0f, 0f, Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat())
        projector.render(batch)
        batch.end()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}