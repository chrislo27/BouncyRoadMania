package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.util.WaveUtils
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Ball(engine: Engine, val beatsPerBounce: Float, sendOutAt: Float, val firstHasSound: Boolean = false) : Entity(engine) {

    data class Bounce(val fromX: Float, val fromY: Float, val fromZ: Float, val toX: Float, val toY: Float, val toZ: Float, val arcHeight: Float,
                      val startBeat: Float, val endBeat: Float, val fromBouncer: Bouncer, val toBouncer: Bouncer?)

    private data class FallOff(val minSeconds: Float, val maxSeconds: Float, val bouncer: Bouncer)

    val sentOutAt: Float = sendOutAt
    var bouncesSoFar: Int = 0
    var bounce: Bounce? = null
    var fellOff: Boolean = false
        private set

    // The ball will "fall off" after this seconds so long if bounce's toBouncer is a player
    private var fallOff: FallOff? = null

    private var started: Boolean = false

    override fun render(batch: SpriteBatch, scale: Float) {
        val tex: Texture = AssetRegistry["tex_ball"]
        val fallOff = fallOff
        if (fallOff != null) {
            if (fallOff.bouncer === engine.redBouncer) {
                batch.setColor(1f, 0f, 0f, 1f)
            } else {
                batch.setColor(1f, 0.9f, 0f, 1f)
            }
        }
        batch.draw(tex, posX - tex.width / 2f, posY, tex.width * 0.5f, tex.height * 0.5f, tex.width * 1f, tex.height * 1f, scale, scale,
                0f, 0, 0, tex.width, tex.height, false, false)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun bounce(from: Bouncer, next: Bouncer, startFromCurrentPos: Boolean,
               startBeat: Float = if (startFromCurrentPos) engine.beat else (sentOutAt + (bouncesSoFar) * beatsPerBounce),
               endBeat: Float = if (fellOff) (startBeat + beatsPerBounce) else (sentOutAt + (bouncesSoFar + 1) * beatsPerBounce)) {
        val fromPos: Entity = if (startFromCurrentPos) this else from
        val fellOff = this.fellOff
        val nextX = if (fellOff) MathUtils.lerp(fromPos.posX, next.posX, 0.5f) else next.posX
        this.bounce = Bounce(fromPos.posX, fromPos.posY, fromPos.posZ, nextX, if (fellOff) -32f else next.posY, next.posZ,
                Interpolation.linear.apply(8f, 96f, beatsPerBounce),
                startBeat, endBeat, from, next)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        val beat = engine.beat
        if (beat < sentOutAt) return

        if (!started) {
            started = true
            if (firstHasSound) {
                bounce?.fromBouncer?.playSound(forcePlay = true)
            }
        }

        val bounce = bounce
        if (bounce != null) {
            val alpha = (beat - bounce.startBeat) / (bounce.endBeat - bounce.startBeat)
            val alphaClamped = alpha.coerceIn(0f, 1f)

            posX = MathUtils.lerp(bounce.fromX, bounce.toX, alphaClamped)
            posY = MathUtils.lerp(bounce.fromY, bounce.toY, alphaClamped) + bounce.arcHeight * WaveUtils.getBounceWave(alphaClamped)
            posZ = MathUtils.lerp(bounce.fromZ, bounce.toZ, alphaClamped)
            posY = if (alphaClamped <= 0.5f) {
                MathUtils.lerp(bounce.fromY, bounce.fromY + bounce.arcHeight, WaveUtils.getBounceWave(alphaClamped))
            } else {
                MathUtils.lerp(bounce.fromY + bounce.arcHeight, bounce.toY, (1f - WaveUtils.getBounceWave(alphaClamped)))
            }

            if (alpha >= 1f) {
                val newFrom = bounce.toBouncer
                val next = engine.bouncers.getOrNull((newFrom?.index ?: -2) + 1)
                if (next == null || fellOff) {
                    this.bounce = null
                    this.kill = true
                    if (fellOff) {
                        AssetRegistry.get<Sound>("sfx_splash").play()
                    }
                } else if (newFrom != null) {
                    if (!newFrom.isPlayer) {
                        bouncesSoFar++
                        if (fallOff == null) {
                            prepareFallOff()
                        }
                        bounce(newFrom, next, false)
                        newFrom.playSound(volume = (engine.entities.count { it is Ball } * 0.9f).coerceIn(0.5f, 1f))
                        newFrom.bounceAnimation()
                    } else {
                        val fo = fallOff
                        if (fo != null) {
                            // Check the fall off time, if it has expired, do a proper fall bounce
                            val currentSeconds = engine.seconds
                            if (currentSeconds > fo.maxSeconds) {
                                // Fall off
                                fellOff = true
                                this.fallOff = null
                                bouncesSoFar++
                                newFrom.playSound(semitone = 0)
                                bounce(newFrom, next, true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun prepareFallOff() {
        val bounce = this.bounce
        if (bounce != null) {
            val newFrom = bounce.toBouncer
            val next = engine.bouncers.getOrNull((newFrom?.index ?: -2) + 1)
            if (next != null && next.isPlayer && this.fallOff == null) {
                // Set the fall off time
                val expectedBeat = sentOutAt + (bouncesSoFar + 1) * beatsPerBounce
                val expectedSeconds = engine.tempos.beatsToSeconds(expectedBeat)
                this.fallOff = FallOff(expectedSeconds - Engine.MAX_OFFSET_SEC, expectedSeconds + Engine.MAX_OFFSET_SEC, next)
            }
        }
    }

    fun onInput(inputType: InputType): Boolean {
        val fo = this.fallOff
        if (fo != null && fo.bouncer is PlayerBouncer) {
            // Pending input
            if (fo.bouncer.inputType == inputType && engine.seconds in fo.minSeconds..fo.maxSeconds) {
                // TODO tell engine an input was received at a certain time for recording
                bouncesSoFar++
                fo.bouncer.playSound()
                this.fallOff = null
                prepareFallOff()
                bounce(fo.bouncer, engine.bouncers[fo.bouncer.index + 1], false)
                return true
            }
        }
//        val next = nextBouncerInput
//        if (next != null && next.inputType == inputType) {
//            val bouncer = engine.getBouncerForInput(inputType)
//            val currentSeconds = engine.clock.seconds
//            val diff = currentSeconds - next.seconds
//            val absDiff = diff.absoluteValue
//            if (absDiff <= Engine.MAX_OFFSET_SEC) {
//                if (absDiff > Engine.BARELY_OFFSET) {
//                    // Miss
//                    next.succeeded = 0
//                } else {
//                    bouncer.playSound()
//                    next.succeeded = 1
//                    // FIXME
//                    println("Bouncer for $inputType - $diff sec")
//                    return true
//                }
//            }
//        }
        return false // TODO
    }
}