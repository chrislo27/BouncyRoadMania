package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


open class Bouncer(engine: Engine) : Entity(engine) {

    var bounceAmt: Float = 0f
        protected set
    protected open val texture: Texture
        get() = AssetRegistry["tex_bouncer_blue"]
    protected open val topPart: Int = 40
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
        val bounceBottom: Float = MathUtils.lerp(0f, topTexture.regionHeight + 4f, bounceAmt)

        // Render bottom first
        batch.draw(bottomTexture, posX - origin.x, posY - (origin.y + bottomTexture.regionHeight * scale) + bounceBottom,
                origin.x, origin.y,
                bottomTexture.regionWidth.toFloat(), bottomTexture.regionHeight.toFloat(),
                scale, scale, 0f)

        batch.draw(topTexture, posX - origin.x, posY - origin.y + bounceTop,
                origin.x, origin.y,
                topTexture.regionWidth.toFloat(), topTexture.regionHeight.toFloat(),
                scale, scale, 0f)

        batch.setColor(0f, 1f, 0f, 1f)
        batch.fillRect(posX - 2f, posY - 2f, 4f, 4f)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        if (bounceAmt > 0f){
            bounceAmt -= delta * 10f
            if (bounceAmt < 0f)
                bounceAmt = 0f
        }
    }

    fun bounce() {
        bounceAmt = 1f
    }

}

class RedBouncer(engine: Engine) : Bouncer(engine) {
    override val texture: Texture
        get() = AssetRegistry["tex_bouncer_red"]

    init {
        origin.x = 24f
    }
}

class YellowBouncer(engine: Engine) : Bouncer(engine) {
    override val texture: Texture
        get() = AssetRegistry["tex_bouncer_yellow"]

    init {
        origin.x = 11f
    }
}
