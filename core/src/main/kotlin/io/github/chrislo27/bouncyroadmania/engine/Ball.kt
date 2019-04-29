package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.util.WaveUtils
import io.github.chrislo27.toolboks.registry.AssetRegistry
import kotlin.math.absoluteValue


class Ball(engine: Engine, val beatsPerBounce: Float) : Entity(engine) {

    data class NextInput(val inputType: InputType, val seconds: Float, var succeeded: Int = -1)

    val sentOutAt: Float = engine.clock.beat
    var bouncerIndex: Int = 0
    var fellOff: Boolean = false

    // The next bouncer input that should be fired at the seconds
    private var nextBouncerInput: NextInput? = null

    override fun render(batch: SpriteBatch, scale: Float) {
        val tex: Texture = AssetRegistry["tex_ball"]
        batch.draw(tex, posX - tex.width / 2f, posY, tex.width * 0.5f, tex.height * 0.5f, tex.width * 1f, tex.height * 1f, scale, scale,
                0f, 0, 0, tex.width, tex.height, false, false)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        val beat = engine.clock.beat
        val estimatedBouncerIndex = ((beat - sentOutAt) / beatsPerBounce).toInt()
        val next = nextBouncerInput
        if (!kill && estimatedBouncerIndex >= engine.bouncers.size - 1) {
            kill = true
            bouncerIndex = engine.bouncers.size - 2
            val toBouncer = engine.bouncers[bouncerIndex + 1]
            if (!toBouncer.isPlayer) {
                toBouncer.bounce()
            }
        } else if (estimatedBouncerIndex < engine.bouncers.size - 1) {
            if (bouncerIndex < estimatedBouncerIndex) {
                if (!fellOff && (next == null || next.succeeded > -1)) {
                    bouncerIndex = estimatedBouncerIndex
                    val bouncer = engine.bouncers[bouncerIndex]
                    if (fellOff) {
                        kill = true
                    } else {
                        if (!bouncer.isPlayer) {
                            bouncer.bounce()
                            bouncer.playSound()
                        } else {
                            val input = engine.getInputTypeForBouncer(bouncer)
                            if (input != null) {
                                nextBouncerInput = NextInput(input, engine.clock.tempos.beatsToSeconds(sentOutAt + (bouncerIndex * beatsPerBounce)))
                            }
                        }
                    }
                    // Handle player errors
                    if (next != null) {
                        if (next.succeeded == 1) {
                            this.nextBouncerInput = null
                        } else {
                            // failed
                            println("fell off")
                            fellOff = true
                            engine.getBouncerForInput(next.inputType).playSound(semitone = 0)
                            this.nextBouncerInput = null

                            AssetRegistry.get<Sound>("sfx_splash").play()
                        }
                    }
                } else if (next != null && next.succeeded == -1) {
                    val currentSeconds = engine.clock.seconds
                    val diff = currentSeconds - next.seconds
                    val absDiff = diff.absoluteValue
                    if (absDiff > Engine.BARELY_OFFSET) {
                        next.succeeded = 0
                    }
                }
            }
        }

        // Set position
        val a = if (next != null && next.succeeded == -1) 0f else (((beat - sentOutAt) - (bouncerIndex * beatsPerBounce)) / beatsPerBounce).coerceIn(0f, 1f)
        val arcHeight = Interpolation.linear.apply(8f, 96f, beatsPerBounce)
        val fromBouncer = engine.bouncers[bouncerIndex]
        val toBouncer = engine.bouncers[bouncerIndex + 1]

        val targetY = if (fellOff) -64f else toBouncer.posY

        posX = MathUtils.lerp(fromBouncer.posX, toBouncer.posX, a)
        posY = MathUtils.lerp(fromBouncer.posY, targetY, a) + arcHeight * WaveUtils.getBounceWave(a)
        posZ = MathUtils.lerp(fromBouncer.posZ, toBouncer.posZ, a)
    }

    fun onInput(inputType: InputType): Boolean {
        val next = nextBouncerInput
        if (next != null && next.inputType == inputType) {
            val bouncer = engine.getBouncerForInput(inputType)
            val currentSeconds = engine.clock.seconds
            val diff = currentSeconds - next.seconds
            val absDiff = diff.absoluteValue
            if (absDiff <= Engine.MAX_OFFSET_SEC) {
                if (absDiff > Engine.BARELY_OFFSET) {
                    // Miss
                    next.succeeded = 0
                } else {
                    bouncer.playSound()
                    next.succeeded = 1
                    // FIXME
                    println("Bouncer for $inputType - $diff sec")
                    return true
                }
            }
        }
        return false // TODO
    }
}