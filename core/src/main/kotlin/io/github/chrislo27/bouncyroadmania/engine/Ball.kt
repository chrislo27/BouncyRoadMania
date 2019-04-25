package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.util.WaveUtils
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Ball(engine: Engine, val beatsPerBounce: Float) : Entity(engine) {

    val sentOutAt: Float = engine.clock.beat
    var bouncerIndex: Int = 0
    private var lastBouncerIndex: Int = -1

    override fun render(batch: SpriteBatch, scale: Float) {
        val tex: Texture = AssetRegistry["tex_ball"]
        batch.draw(tex, posX - tex.width / 2f, posY, tex.width * 0.5f, tex.height * 0.5f, tex.width * 1f, tex.height * 1f, scale, scale,
                0f, 0, 0, tex.width, tex.height, false, false)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        val beat = engine.clock.beat
        bouncerIndex = ((beat - sentOutAt) / beatsPerBounce).toInt()
        if (bouncerIndex >= engine.bouncers.size - 1) {
            // FIXME
            kill = true
            bouncerIndex = engine.bouncers.size - 2
            val toBouncer = engine.bouncers[bouncerIndex + 1]
            toBouncer.bounce()
        }

        // Set position
        val a = (((beat - sentOutAt) - (bouncerIndex * beatsPerBounce)) / beatsPerBounce).coerceIn(0f, 1f)
        val arcHeight = Interpolation.linear.apply(8f, 96f, beatsPerBounce)
        val fromBouncer = engine.bouncers[bouncerIndex]
        val toBouncer = engine.bouncers[bouncerIndex + 1]

        if (bouncerIndex != lastBouncerIndex) {
            lastBouncerIndex = bouncerIndex
            fromBouncer.bounce()
        }

        posX = MathUtils.lerp(fromBouncer.posX, toBouncer.posX, a)
        posY = MathUtils.lerp(fromBouncer.posY, toBouncer.posY, a) + arcHeight * WaveUtils.getBounceWave(a)
        posZ = MathUtils.lerp(fromBouncer.posZ, toBouncer.posZ, a)
    }
}