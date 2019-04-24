package io.github.chrislo27.bouncyroadmania.renderer

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.util.*

/**
 * A decal-like projector that uses PaperSprites.
 */
open class PaperProjection(var scaleCoeff: Float = 1.0f) {

    val comparator: Comparator<PaperRenderable>
        get() = PaperRenderableComparator

    open fun render(batch: SpriteBatch, sprites: MutableList<out PaperRenderable>) {
        // ensure Z-order
        sprites.sortWith(comparator)

        sprites.forEach { able ->
            if (able.posZ >= 0f) {
                able.render(batch, scaleCoeff)
            }
        }
    }

}

class PaperSprite : Sprite, PaperRenderable {

    override var posX: Float
        get() = this.x
        set(value) {
            this.x = value
        }
    override var posY: Float
        get() = this.y
        set(value) {
            this.y = value
        }
    override var posZ: Float = 1f

    constructor() : super()
    constructor(texture: Texture?) : super(texture)
    constructor(texture: Texture?, srcWidth: Int, srcHeight: Int) : super(texture, srcWidth, srcHeight)
    constructor(texture: Texture?, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
            : super(texture, srcX, srcY, srcWidth, srcHeight)
    constructor(region: TextureRegion?) : super(region)
    constructor(region: TextureRegion?, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
            : super(region, srcX, srcY, srcWidth, srcHeight)
    constructor(sprite: Sprite?) : super(sprite)

    init {
        setOriginCenter()
    }

    fun setZ(z: Float): PaperSprite {
        this.posZ = z
        return this
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        return setZ(z).setPosition(x, y)
    }

    override fun render(batch: SpriteBatch, scale: Float) {
        val oldScaleX = scaleX
        val oldScaleY = scaleY

        if (posZ < 0f) {
            return
        }
        val realZ = posZ + 1f

        // set new projection scale
        setScale((oldScaleX / realZ) * scale, (oldScaleY / realZ) * scale)

        // render
        draw(batch)

        // reset scale
        setScale(oldScaleX, oldScaleY)
    }

}

interface PaperRenderable {

    var posX: Float
    var posY: Float
    var posZ: Float

    fun render(batch: SpriteBatch, scale: Float)

}

/**
 * Compares [PaperRenderable]s, higher [PaperRenderable.posZ] are first when sorted
 */
object PaperRenderableComparator : Comparator<PaperRenderable> {

    override fun compare(o1: PaperRenderable?, o2: PaperRenderable?): Int {
        if (o1 == null) {
            return if (o2 == null)
                0
            else
                -1
        }
        if (o2 == null)
            return 1

        if (o1.posZ > o2.posZ)
            return -1

        if (o1.posZ < o2.posZ)
            return 1

        return 0
    }

}
