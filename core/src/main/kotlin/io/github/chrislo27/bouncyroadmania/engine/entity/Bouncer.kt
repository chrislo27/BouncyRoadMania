package io.github.chrislo27.bouncyroadmania.engine.entity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSound
import io.github.chrislo27.bouncyroadmania.util.Semitones
import io.github.chrislo27.toolboks.registry.AssetRegistry


open class Bouncer(engine: Engine) : Entity(engine) {

    companion object {
        protected val TMP_ARRAY: FloatArray = FloatArray(3)
    }
    
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
    protected open val tint: Color get() = engine.normalBouncerCurrentTint
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

//        batch.color = tint
        batch.setColor(1f, 1f, 1f, 1f)
        
        // FIXME optimize shaders
        val currentShader = batch.shader
        val hsvShader = BRManiaApp.instance.hsvShader
        batch.shader = hsvShader
        
        tint.toHsv(TMP_ARRAY)
        hsvShader.setUniformf("v_hsva", TMP_ARRAY[0], TMP_ARRAY[1], TMP_ARRAY[2], tint.a)
        
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
        
        batch.shader = currentShader

//        batch.setColor(0f, 1f, 0f, 1f)
//        batch.fillRect(posX - 2f, posY - 2f, 4f, 4f)
//        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        if (bounceAmt > 0f) {
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
        get() = engine.dpadBouncerCurrentTint

    init {
        origin.x = 24f
    }
}

class YellowBouncer(engine: Engine) : PlayerBouncer(engine, InputType.A) {
    override val texture: Texture
        get() = AssetRegistry["tex_bouncer_yellow"]
    override val semitone: Int = -4
    override val tint: Color
        get() = engine.aBouncerCurrentTint

    init {
        origin.x = 11f
    }
}

object BouncerShaders {
    val vertex = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_texCoords;

void main()
{
   v_color = ${ShaderProgram.COLOR_ATTRIBUTE};
   // v_color.a = v_color.a * (255.0/254.0);
   v_texCoords = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
   gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}
"""
    val frag = """

varying vec4 v_color;
varying vec2 v_texCoords;
uniform vec4 v_hsva;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 textureColor = texture2D(u_texture, v_texCoords);
    vec4 fragRGBA = textureColor.rgba;
    vec3 fragRGB = vec3(fragRGBA.xyz);
    vec3 fragHSV = rgb2hsv(fragRGB).xyz;
    fragHSV.x = v_hsva.x / 360.0;
    fragHSV.yz *= v_hsva.yz;
    fragHSV.xyz = mod(fragHSV.xyz, 1.0);
    fragRGB = hsv2rgb(fragHSV);
    gl_FragColor = vec4(fragRGB, textureColor.w * v_hsva.w);
} 
"""
}
