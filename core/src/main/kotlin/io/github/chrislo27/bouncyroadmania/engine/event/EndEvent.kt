package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EndEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val canBeCopied: Boolean = false

    override fun render(batch: SpriteBatch, editor: Editor) {
        val oldColor = batch.packedColor
        val theme = editor.theme
        val selectionTint = theme.events.selectionTint

        val x = bounds.x + lerpDifference.x
        val y = bounds.y + lerpDifference.y
        val height = bounds.height + lerpDifference.height
        val width = bounds.width + lerpDifference.width

        batch.setColorWithTintIfNecessary(selectionTint, theme.trackLine)
        batch.fillRect(x, y, width * 0.125f, height)
        batch.fillRect(x + width, y, width * -0.5f, height)

        batch.packedColor = oldColor
    }

    override fun copy(): Nothing {
        error("This event cannot be copied")
    }
}