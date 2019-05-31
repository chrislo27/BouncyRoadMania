package io.github.chrislo27.bouncyroadmania.engine.event

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.event.BgImageEvent.Companion.getAlpha
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.util.TrueCheckbox
import java.lang.ref.WeakReference
import java.util.*


class SpotlightEvent(engine: Engine, instantiator: Instantiator)
    : InstantiatedEvent(engine, instantiator) {

    enum class SpotlightTarget {
        A, DPAD;
        companion object {
            val VALUES = values().toList()
            val BITS: Map<SpotlightTarget, Int> = VALUES.associateWith { VALUES.indexOf(it) }
        }
    }
    
    override val canBeCopied: Boolean = true
    override val hasEditableParams: Boolean = true
    override val isStretchable: Boolean = true
    
    var fadeTransitions: Boolean = true
    val targets: EnumSet<SpotlightTarget> = EnumSet.allOf(SpotlightTarget::class.java)

    init {
        bounds.width = 1f
    }

    override fun copy(): SpotlightEvent {
        return SpotlightEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.fadeTransitions = this.fadeTransitions
        }
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): SpotlightEventParamsStage {
        return SpotlightEventParamsStage(stage)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        fadeTransitions = node["fadeTransitions"]?.asBoolean(true) ?: true
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        node.put("fadeTransitions", fadeTransitions)
        node.put("targets", targets.fold(0) { res, it ->
            res or (1 shl SpotlightTarget.BITS.getValue(it))
        })
    }

    fun getDarknessAlpha(): Float = this.getAlpha(this.fadeTransitions)

    inner class SpotlightEventParamsStage(parent: EditorStage) : EventParamsStage<SpotlightEvent>(parent, this@SpotlightEvent) {

        init {
            contentStage.elements += object : TrueCheckbox<EditorScreen>(palette, contentStage, contentStage) {
                private val thisCheckbox: TrueCheckbox<EditorScreen> = this
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    parent.editor.mutate(object : ReversibleAction<Editor> {
                        val checkbox: WeakReference<TrueCheckbox<EditorScreen>> = WeakReference(thisCheckbox)
                        val value = checked
                        override fun redo(context: Editor) {
                            event.fadeTransitions = value
                            checkbox.get()?.checked = value
                        }

                        override fun undo(context: Editor) {
                            event.fadeTransitions = !value
                            checkbox.get()?.checked = !value
                        }
                    })
                }
            }.apply {
                this.checked = event.fadeTransitions
                this.textLabel.isLocalizationKey = true
                this.textLabel.text = "spotlightEvent.fadeTransitions"
                this.location.set(screenHeight = 0.1f, screenY = 0.9f)
            }

            this.updatePositions()
        }
    }
    
}