package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator


class SkillStarEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isUnique: Boolean = true

    init {
        bounds.width = 0.5f
    }

    override fun getRenderColor(editor: Editor, theme: EditorTheme): Color {
        return theme.events.skillStar
    }

    override fun copy(): SkillStarEvent {
        return SkillStarEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

}