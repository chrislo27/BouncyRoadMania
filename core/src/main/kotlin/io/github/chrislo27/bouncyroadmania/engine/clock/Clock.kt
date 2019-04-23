package io.github.chrislo27.bouncyroadmania.engine.clock

import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChanges


class Clock {

    val tempos: TempoChanges = TempoChanges(120f)

    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(value)
        }
    var beat: Float = 0f
        private set

    fun update(delta: Float) {

    }

}