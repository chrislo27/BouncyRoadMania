package io.github.chrislo27.bouncyroadmania.engine.event.practice

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.Event


class SpawnTextBoxEvent(engine: Engine, val textBox: TextBox) : Event(engine) {
    
    override val canBeCopied: Boolean = false

    override fun copy(): Event {
        throw NotImplementedError("This event cannot be copied")
    }

    override fun onStart() {
        engine.currentTextBox = textBox.copy()
        if (textBox.requiresInput) {
            engine.playState = PlayState.PAUSED
        }
    }

    override fun onEnd() {
        if (!textBox.requiresInput) {
            engine.currentTextBox = null
        }
    }
}