package io.github.chrislo27.bouncyroadmania.engine.entity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.input.InputResult
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSound
import io.github.chrislo27.bouncyroadmania.util.WaveUtils
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Ball(engine: Engine, val beatsPerBounce: Float, sendOutAt: Float, val firstHasSound: Boolean = false, val color: Color = Color(1f, 1f, 1f, 1f))
    : Entity(engine) {

    data class Bounce(val fromX: Float, val fromY: Float, val fromZ: Float, val toX: Float, val toY: Float, val toZ: Float, val arcHeight: Float,
                      val startBeat: Float, val endBeat: Float, val fromBouncer: Bouncer, val toBouncer: Bouncer?)

    private data class FallOff(val perfectSeconds: Float, val minSeconds: Float, val maxSeconds: Float, val bouncer: Bouncer)

    val sentOutAt: Float = sendOutAt
    var bouncesSoFar: Int = 0
    var bounce: Bounce? = null
    private var fellOff: FallOff? = null
    val didFallOff: Boolean get() = fellOff != null

    // The ball will "fall off" after this seconds so long if bounce's toBouncer is a player
    private var fallOff: FallOff? = null

    private var started: Boolean = false

    override fun render(batch: SpriteBatch, scale: Float) {
        val tex: Texture = AssetRegistry["tex_ball"]
//        val fallOff = fallOff
//        if (fallOff != null) {
//            if (fallOff.bouncer === engine.redBouncer) {
//                batch.setColor(1f, 0f, 0f, 1f)
//            } else {
//                batch.setColor(1f, 0.9f, 0f, 1f)
//            }
//        }
        batch.color = color
        batch.draw(tex, posX - tex.width / 2f, posY, tex.width * 0.5f, tex.height * 0.5f, tex.width * 1f, tex.height * 1f, scale, scale,
                0f, 0, 0, tex.width, tex.height, false, false)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun startOff(firstBouncer: Bouncer = engine.bouncers.first()) {
        posX = firstBouncer.posX
        posY = firstBouncer.posY
        bounce(firstBouncer, engine.bouncers[firstBouncer.index + 1], false)
    }

    fun bounce(from: Bouncer, next: Bouncer, startFromCurrentPos: Boolean,
               startBeat: Float = if (startFromCurrentPos) engine.beat else (sentOutAt + (bouncesSoFar) * beatsPerBounce),
               endBeat: Float = if (fellOff != null) (startBeat + beatsPerBounce) else (sentOutAt + (bouncesSoFar + 1) * beatsPerBounce)) {
        val fromPos: Entity = if (startFromCurrentPos) this else from
        val fellOff = this.fellOff
        val nextX = if (fellOff != null) MathUtils.lerp(fromPos.posX, next.posX, 0.5f) else next.posX
        this.bounce = Bounce(fromPos.posX, fromPos.posY, fromPos.posZ, nextX, if (fellOff != null) -32f else next.posY, next.posZ,
                100f * beatsPerBounce + 8f,
                startBeat, endBeat, from, next)
    }

    override fun renderUpdate(delta: Float) {
        super.renderUpdate(delta)
        val beat = engine.beat
        if (beat < sentOutAt) return

        if (!started) {
            started = true
            if (firstHasSound) {
                val from = bounce?.fromBouncer
                if (from != null) {
                    if (!MathUtils.isEqual(engine.lastBounceTinkSound[from.soundHandle] ?: Float.NEGATIVE_INFINITY, engine.seconds, 0.05f)) {
                        engine.lastBounceTinkSound[from.soundHandle] = engine.seconds
                        from.playSound(forcePlay = true)
                    }
                }
            }
        }

        val bounce = bounce
        if (bounce != null) {
            val alpha = (beat - bounce.startBeat) / (bounce.endBeat - bounce.startBeat)
            val alphaClamped = alpha.coerceIn(0f, 1f)

            posX = MathUtils.lerp(bounce.fromX, bounce.toX, alphaClamped)
            posY = MathUtils.lerp(bounce.fromY, bounce.toY, alphaClamped) + bounce.arcHeight * WaveUtils.getBounceWave(alphaClamped)
            posZ = MathUtils.lerp(bounce.fromZ, bounce.toZ, alphaClamped) - 0.001f
            posY = if (alphaClamped <= 0.5f) {
                MathUtils.lerp(bounce.fromY, bounce.fromY + bounce.arcHeight, WaveUtils.getBounceWave(alphaClamped))
            } else {
                MathUtils.lerp(bounce.fromY + bounce.arcHeight, bounce.toY, (1f - WaveUtils.getBounceWave(alphaClamped)))
            }

            if (alpha >= 1f) {
                val newFrom = bounce.toBouncer
                val intAlpha = alpha.toInt()
                val next = engine.bouncers.getOrNull((newFrom?.index ?: -2) + intAlpha)
                val fellOff = this.fellOff
                if (next == null || fellOff != null) {
                    this.bounce = null
                    this.kill = true
                    if (fellOff != null) {
                        AssetRegistry.get<BeadsSound>("sfx_splash").play()
                        if (engine.requiresPlayerInput) {
                            val inputSecs = engine.seconds
                            engine.inputResults += InputResult((fellOff.bouncer as? PlayerBouncer)?.inputType ?: InputType.A,
                                    inputSecs - fellOff.perfectSeconds, ((inputSecs - fellOff.perfectSeconds) / Engine.MAX_OFFSET_SEC).coerceIn(-1f, 1f))
                        }
                    }
                } else if (newFrom != null) {
                    if (!newFrom.isPlayer || !engine.requiresPlayerInput) {
                        bouncesSoFar += intAlpha
                        if (fallOff == null) {
                            prepareFallOff(intAlpha)
                        }
                        bounce(newFrom, next, false)
                        if (newFrom.isPlayer) {
                            newFrom.playSound()
                        } else if (!MathUtils.isEqual(engine.lastBounceTinkSound[newFrom.soundHandle]
                                        ?: Float.NEGATIVE_INFINITY, engine.seconds, 0.05f)) {
                            if (!newFrom.isSilent) {
                                engine.lastBounceTinkSound[newFrom.soundHandle] = engine.seconds
                            }
                            newFrom.playSound()
                        }
//                        if (newFrom.soundHandle == "sfx_tink") {
//                            if (!MathUtils.isEqual(engine.lastBounceTinkSound, engine.seconds, 0.05f)) {
//                                engine.lastBounceTinkSound = engine.seconds
//                                newFrom.playSound()
//                            }
//                        } else {
//                            newFrom.playSound()
//                        }
                        newFrom.bounceAnimation()
                    } else {
                        val fo = fallOff
                        if (fo != null) {
                            // Check the fall off time, if it has expired, do a proper fall bounce
                            val currentSeconds = engine.seconds
                            if (currentSeconds > fo.maxSeconds) {
                                // Fall off
                                this.fellOff = fo
                                this.fallOff = null
                                bouncesSoFar += intAlpha
                                newFrom.playSound(semitone = 0)
                                bounce(newFrom, next, true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun prepareFallOff(increment: Int = 1) {
        val bounce = this.bounce
        if (bounce != null) {
            val newFrom = bounce.toBouncer
            val next = engine.bouncers.getOrNull((newFrom?.index ?: -2) + increment)
            if (next != null && next.isPlayer && this.fallOff == null && engine.requiresPlayerInput) {
                // Set the fall off time
                val expectedBeat = sentOutAt + (bouncesSoFar + 1) * beatsPerBounce
                val expectedSeconds = engine.tempos.beatsToSeconds(expectedBeat)
                this.fallOff = FallOff(expectedSeconds, expectedSeconds - Engine.MAX_OFFSET_SEC, expectedSeconds + Engine.MAX_OFFSET_SEC, next)
            }
        }
    }

    fun onInput(inputType: InputType): Boolean {
        val fo = this.fallOff
        if (fo != null && fo.bouncer is PlayerBouncer) {
            // Pending input
            val inputSecs = engine.seconds
            if (fo.bouncer.inputType == inputType && inputSecs in fo.minSeconds..fo.maxSeconds) {
                val inputResult = InputResult(fo.bouncer.inputType, inputSecs - fo.perfectSeconds, (inputSecs - fo.perfectSeconds) / Engine.MAX_OFFSET_SEC)
                engine.inputResults += inputResult
//                println("Got input for ${inputResult.type} - ${inputResult.accuracyPercent} - ${inputResult.inputScore}")
                bouncesSoFar++
                fo.bouncer.playSound()
                this.fallOff = null
                prepareFallOff()
                bounce(fo.bouncer, engine.bouncers[fo.bouncer.index + 1], false)
                return true
            }
        }
        return false // TODO
    }
}