package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.entity.Ball
import io.github.chrislo27.bouncyroadmania.registry.Instantiator


class DeployEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true

    init {
        this.bounds.width = 0.5f
    }

    override fun getRenderColor(editor: Editor, theme: EditorTheme): Color {
        return theme.events.input
    }

    override fun onStart() {
        engine.entities += Ball(engine, this.bounds.width, this.bounds.x).apply {
            startOff()
        }
    }

    override fun copy(): DeployEvent {
        return DeployEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

}