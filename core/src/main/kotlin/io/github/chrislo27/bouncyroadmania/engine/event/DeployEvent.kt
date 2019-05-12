package io.github.chrislo27.bouncyroadmania.engine.event

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.entity.Ball
import io.github.chrislo27.bouncyroadmania.registry.Instantiator


class DeployEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override fun onStart() {
        engine.entities += Ball(engine, this.bounds.width, this.bounds.x)
    }

    override fun copy(): DeployEvent {
        return DeployEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

}