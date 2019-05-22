package io.github.chrislo27.bouncyroadmania.engine.event.practice

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.InstantiatedEvent
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import java.lang.ref.WeakReference


class SpawnTextBoxEvent(engine: Engine, var textBox: TextBox, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true
    override val canBeCopied: Boolean = true
    private var spawnedIn: WeakReference<TextBox>? = null

    override fun copy(): SpawnTextBoxEvent {
        return SpawnTextBoxEvent(engine, textBox.copy(), instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        textBox = TextBox(node["text"]?.asText() ?: "<missing text>", node["requiresInput"]?.asBoolean() ?: false)
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        node.put("text", textBox.text)
        node.put("requiresInput", textBox.requiresInput)
    }

    override fun onStart() {
        val copy = textBox.copy()
        engine.currentTextBox = copy
        spawnedIn = WeakReference(copy)
        if (copy.requiresInput) {
            engine.playState = PlayState.PAUSED
        }
    }

    override fun onEnd() {
        val current = spawnedIn?.get()
        if (current != null && engine.currentTextBox === current) {
            engine.currentTextBox = null
        }
    }
}