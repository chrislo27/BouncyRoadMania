package io.github.chrislo27.bouncyroadmania.engine

import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChanges


open class Clock {

    val tempos: TempoChanges = TempoChanges(120f)

    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(value)
        }
    var beat: Float = 0f
        private set
    open var playState: PlayState = PlayState.STOPPED

    open fun update(delta: Float) {
        if (playState != PlayState.PLAYING) return

        seconds += delta
    }

}