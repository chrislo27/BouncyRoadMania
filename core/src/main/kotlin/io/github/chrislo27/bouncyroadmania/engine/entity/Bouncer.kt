package io.github.chrislo27.bouncyroadmania.engine.entity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSound
import io.github.chrislo27.bouncyroadmania.util.Semitones
import io.github.chrislo27.toolboks.registry.AssetRegistry


open class Bouncer(engine: Engine) : Entity(engine) {

    var bounceAmt: Float = 0f
        protected set
    open val isPlayer: Boolean = false
    open val semitone: Int = 0
    var isSilent: Boolean = false
    var soundHandle: String = "sfx_tink"
    val index: Int get() = engine.bouncers.indexOf(this)

    protected open val texture: Texture
        get() = AssetRegistry["tex_bouncer_blue"]
    protected open val topPart: Int = 40
    protected open val tint: Color get() = engine.normalBouncerTint
    protected val topTexture: TextureRegion by lazy {
        val tex = texture
        TextureRegion(tex, 0, 0, tex.width, topPart)
    }
    protected val bottomTexture: TextureRegion by lazy {
        val tex = texture
        TextureRegion(tex, 0, topPart, tex.width, tex.height - topPart)
    }
    protected val origin: Vector2 = run {
        val tex = texture
        Vector2(tex.width * 0.5f, topTexture.regionHeight * 1f)
    }
    
    override fun render(batch: SpriteBatch, scale: Float) {
        // Position is based on centre, top

        val bounceTop: Float = MathUtils.lerp(0f, 8f, bounceAmt)
        val bounceBottom: Float = MathUtils.lerp(0f, topTexture.regionHeight * scale, bounceAmt)

        batch.color = tint
        
        // Render bottom first
        batch.draw(bottomTexture, posX - origin.x, posY - (origin.y + bottomTexture.regionHeight * scale) + bounceBottom,
                origin.x, origin.y,
                bottomTexture.regionWidth.toFloat(), bottomTexture.regionHeight.toFloat(),
                scale, scale, 0f)

        batch.draw(topTexture, posX - origin.x, posY - origin.y + bounceTop,
                origin.x, origin.y,
                topTexture.regionWidth.toFloat(), topTexture.regionHeight.toFloat(),
                scale, scale, 0f)
        
        batch.setColor(1f, 1f, 1f, 1f)

//        batch.setColor(0f, 1f, 0f, 1f)
//        batch.fillRect(posX - 2f, posY - 2f, 4f, 4f)
//        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        if (bounceAmt > 0f){
            bounceAmt -= delta * 8f
            if (bounceAmt < 0f)
                bounceAmt = 0f
        }
    }

    fun bounceAnimation() {
        bounceAmt = 1f
    }

    fun playSound(semitone: Int = this.semitone, volume: Float = 1f, forcePlay: Boolean = false, semitoneAdd: Int = 0) {
        if (!isSilent || forcePlay) {
            val width = engine.camera.viewportWidth
            AssetRegistry.get<BeadsSound>(soundHandle).play(loop = false, volume = (1f - posZ).coerceIn(0.75f, 1f) * volume, pitch = Semitones.getALPitch(semitone + semitoneAdd)/*, position = ((posX - (width / 2f)) / (width * 1.25)).coerceIn(-1.0, 1.0)*/)
        }
    }

}

abstract class PlayerBouncer(engine: Engine, val inputType: InputType) : Bouncer(engine) {
    override val isPlayer: Boolean = true
}

class RedBouncer(engine: Engine) : PlayerBouncer(engine, InputType.DPAD) {
    override val texture: Texture
        get() = AssetRegistry["tex_bouncer_red"]
    override val semitone: Int = 3
    override val tint: Color
        get() = engine.dpadBouncerTint

    init {
        origin.x = 24f
    }
}

class YellowBouncer(engine: Engine) : PlayerBouncer(engine, InputType.A) {
    override val texture: Texture
        get() = AssetRegistry["tex_bouncer_yellow"]
    override val semitone: Int = -4
    override val tint: Color
        get() = engine.aBouncerTint

    init {
        origin.x = 11f
    }
}
