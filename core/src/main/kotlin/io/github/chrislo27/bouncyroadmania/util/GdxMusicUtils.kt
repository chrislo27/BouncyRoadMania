package io.github.chrislo27.bouncyroadmania.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.bouncyroadmania.BRManiaApp


fun Music.fadeTo(volume: Float, duration: Float, stopIfZero: Boolean = true) {
    val originalVolume = this.volume
    val newVolume = volume.coerceIn(0f, 1f)
    if (duration <= 0f || originalVolume == volume || !this.isPlaying) {
        this.volume = volume
        if (newVolume <= 0f && stopIfZero) {
            this.stop()
        }
        return
    }
    val startSeconds = BRManiaApp.instance.secondsElapsed
    fun createRunnable(): Runnable {
        return Runnable {
            val alpha = ((BRManiaApp.instance.secondsElapsed - startSeconds) / duration).coerceIn(0f, 1f)
            val v = MathUtils.lerp(originalVolume, newVolume, alpha)
            this.volume = v
            if (alpha < 1f) {
                Gdx.app.postRunnable(createRunnable())
            } else {
                if (newVolume <= 0f && stopIfZero) {
                    this.stop()
                }
            }
        }
    }
    Gdx.app.postRunnable(createRunnable())
}

