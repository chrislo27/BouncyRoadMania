package io.github.chrislo27.bouncyroadmania.engine.event

import io.github.chrislo27.bouncyroadmania.engine.Engine


open class SimpleEvent(engine: Engine, val onStartFunc: SimpleEvent.() -> Unit,
                       val whilePlayingFunc: SimpleEvent.() -> Unit = {}, val onEndFunc: SimpleEvent.() -> Unit = {})
    : Event(engine) {
    
    override val canBeCopied: Boolean = false

    override fun copy(): Event {
        throw NotImplementedError("This event cannot be copied")
    }

    override fun onStart() {
        this.onStartFunc()
    }

    override fun whilePlaying() {
        this.whilePlayingFunc()
    }

    override fun onEnd() {
        this.onEndFunc()
    }
}