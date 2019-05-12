package io.github.chrislo27.bouncyroadmania.engine.event

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator


abstract class InstantiatedEvent(engine: Engine, val instantiator: Instantiator) : Event(engine)