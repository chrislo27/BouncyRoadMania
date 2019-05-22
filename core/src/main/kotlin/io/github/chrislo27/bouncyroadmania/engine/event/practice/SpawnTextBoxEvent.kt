package io.github.chrislo27.bouncyroadmania.engine.event.practice

import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.InstantiatedEvent
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.util.TrueCheckbox
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel
import java.lang.ref.WeakReference


class SpawnTextBoxEvent(engine: Engine, var textBox: TextBox, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true
    override val canBeCopied: Boolean = true
    override val hasEditableParams: Boolean = true
    private var spawnedIn: WeakReference<TextBox>? = null

    override fun copy(): SpawnTextBoxEvent {
        return SpawnTextBoxEvent(engine, textBox.copy(), instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): SpawnTextBoxEventParamsStage {
        return SpawnTextBoxEventParamsStage(stage)
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

    inner class SpawnTextBoxEventParamsStage(parent: EditorStage) : EventParamsStage<SpawnTextBoxEvent>(parent, this@SpawnTextBoxEvent) {
        init {
            val size = 48f
            contentStage.elements += object : TrueCheckbox<EditorScreen>(palette, contentStage, contentStage) {
                private val thisCheckbox: TrueCheckbox<EditorScreen> = this
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    parent.editor.mutate(object : ReversibleAction<Editor> {
                        val checkbox: WeakReference<TrueCheckbox<EditorScreen>> = WeakReference(thisCheckbox)
                        val value = checked
                        override fun redo(context: Editor) {
                            event.textBox.requiresInput = value
                            checkbox.get()?.checked = value
                        }

                        override fun undo(context: Editor) {
                            event.textBox.requiresInput = !value
                            checkbox.get()?.checked = !value
                        }
                    })
                }
            }.apply {
                this.checked = event.textBox.requiresInput
                this.textLabel.isLocalizationKey = true
                this.textLabel.text = "textBoxEvent.requiresPlayerInput"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size))
            }
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "textBoxEvent.text"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 2))
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "textBoxEvent.text.max"
            }
            contentStage.elements += object : TextField<EditorScreen>(palette, contentStage, contentStage) {
                override fun onEnterPressed(): Boolean {
                    hasFocus = false
                    textBox = textBox.copy(text = this.text)
                    return true
                }
            }.apply {
                this.background = true
                this.canInputNewlines = true
                this.canPaste = true
                this.textAlign = Align.left
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 3))
            }
        }
    }
    
}