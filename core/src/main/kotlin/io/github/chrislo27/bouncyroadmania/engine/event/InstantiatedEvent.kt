package io.github.chrislo27.bouncyroadmania.engine.event

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator


abstract class InstantiatedEvent(engine: Engine, val instantiator: Instantiator) : Event(engine) {

    override val renderText: String get() = instantiator.displayName
    open val isUnique: Boolean = false
    override val canBeCopied: Boolean get() = super.canBeCopied && !isUnique

    open fun fromJson(node: ObjectNode) {
        bounds.set(node["x"]?.floatValue() ?: 0f, node["y"]?.floatValue() ?: 0f, node["w"]?.floatValue() ?: 1f, node["h"]?.floatValue()?.coerceAtLeast(1f) ?: 1f)
    }

    open fun toJson(node: ObjectNode) {
        node.put("i", instantiator.id)
        node.put("x", bounds.x)
        node.put("y", bounds.y)
        node.put("w", bounds.width)
        node.put("h", bounds.height)
    }

}