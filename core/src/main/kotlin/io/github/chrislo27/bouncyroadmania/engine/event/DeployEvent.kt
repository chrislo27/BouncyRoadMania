package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.entity.Ball
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.util.TrueCheckbox
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import java.lang.ref.WeakReference


class DeployEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    var firstBounceHasSound: Boolean = false

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

    override fun createParamsStage(editor: Editor, stage: EditorStage): EventParamsStage<DeployEvent> {
        return DeployEventParamsStage(stage)
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
        engine.entities += Ball(engine, this.bounds.width, this.bounds.x, firstBounceHasSound).apply {
            startOff()
        }
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        firstBounceHasSound = node["firstBounceHasSound"]?.asBoolean(false) ?: false
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        if (firstBounceHasSound) {
            node.put("firstBounceHasSound", firstBounceHasSound)
        }
    }

    override fun copy(): DeployEvent {
        return DeployEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.firstBounceHasSound = this.firstBounceHasSound
        }
    }
    
    inner class DeployEventParamsStage(parent: EditorStage) : EventParamsStage<DeployEvent>(parent, this@DeployEvent) {
        init {
            val size = 48f
            val padding = 16f
            contentStage.elements += object : TrueCheckbox<EditorScreen>(palette, contentStage, contentStage) {
                private val thisCheckbox: TrueCheckbox<EditorScreen> = this
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    parent.editor.mutate(object : ReversibleAction<Editor> {
                        val checkbox: WeakReference<TrueCheckbox<EditorScreen>> = WeakReference(thisCheckbox)
                        val value = checked
                        override fun redo(context: Editor) {
                            event.firstBounceHasSound = value
                            checkbox.get()?.checked = value
                        }

                        override fun undo(context: Editor) {
                            event.firstBounceHasSound = !value
                            checkbox.get()?.checked = !value
                        }
                    })
                }
            }.apply {
                this.checked = event.firstBounceHasSound
                this.textLabel.isLocalizationKey = true
                this.textLabel.text = "deployEvent.firstBounceHasSound"
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "deployEvent.firstBounceHasSound.tooltip"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size))
            }
        }
    }

}