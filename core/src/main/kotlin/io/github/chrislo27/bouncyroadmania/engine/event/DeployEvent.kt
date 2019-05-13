package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.entity.Ball
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.maxX


class DeployEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true

    init {
        this.bounds.width = 0.5f
    }

    override fun getRenderColor(editor: Editor, theme: EditorTheme): Color {
        return theme.events.input
    }

    override fun inRenderRange(start: Float, end: Float): Boolean {
        return bounds.x + lerpDifference.x + bounds.width * (engine.bouncers.size - 1f).coerceAtLeast(1f) + lerpDifference.width >= start
                && bounds.x + lerpDifference.x <= end
    }

    override fun getUpperUpdateableBound(): Float {
        return bounds.maxX + (engine.bouncers.size - 1f).coerceAtLeast(0f)
    }

    override fun renderBeforeText(editor: Editor, batch: SpriteBatch) {
        super.renderBeforeText(editor, batch)
        val batchColor = batch.color
        val fill = getRenderColor(editor, editor.theme)
        for (i in 1 until engine.bouncers.size - 1) {
            when (engine.bouncers[i]) {
                engine.yellowBouncer -> batch.setColor(1f, 0.9f, 0.1f, fill.a * 0.25f)
                engine.redBouncer -> batch.setColor(1f, 0f, 0f, fill.a * 0.25f)
                else -> batch.setColor(fill.r, fill.g, fill.b, fill.a * 0.25f)
            }
            batch.fillRect(bounds.x + lerpDifference.x + i * bounds.width, bounds.y + lerpDifference.y,
                    bounds.width, bounds.height)
            batch.setColor(batchColor.r, batchColor.g, batchColor.b, batchColor.a * 0.75f)
            batch.drawRect(bounds.x + lerpDifference.x + i * bounds.width, bounds.y + lerpDifference.y,
                    bounds.width, bounds.height,
                    editor.renderer.toScaleX(BORDER), editor.renderer.toScaleY(BORDER))
        }
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