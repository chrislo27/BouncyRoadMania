package io.github.chrislo27.bouncyroadmania.engine.event.practice

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.event.Event


class XMoreTimesEvent(engine: Engine, val times: Int) : Event(engine) {
    override val canBeCopied: Boolean = false

    override fun copy(): Event {
        throw NotImplementedError("This event cannot be copied")
    }

    override fun onStart() {
        engine.xMoreTimes = times
    }
}