package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.stage.ColourPicker
import io.github.chrislo27.bouncyroadmania.util.fromJsonString
import io.github.chrislo27.bouncyroadmania.util.toJsonString
import io.github.chrislo27.toolboks.ui.TextLabel


class SpotlightEvent(engine: Engine, instantiator: Instantiator)
    : InstantiatedEvent(engine, instantiator) {
    
    override val canBeCopied: Boolean = true
    override val hasEditableParams: Boolean = true
    override val isStretchable: Boolean = true
    
    val shadow: Color = Color(0f, 0f, 0f, 238f / 255f)

    init {
        bounds.width = 1f
    }

    override fun copy(): SpotlightEvent {
        return SpotlightEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.shadow.set(this.shadow)
        }
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): SpotlightEventParamsStage {
        return SpotlightEventParamsStage(stage)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        shadow.fromJsonString(node["shadow"]?.asText())
    }

    override fun toJson(node: ObjectNode) {
        node.put("shadow", shadow.toJsonString())
    }

    inner class SpotlightEventParamsStage(parent: EditorStage) : EventParamsStage<SpotlightEvent>(parent, this@SpotlightEvent) {

        init {
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.textWrapping = false
                this.text = "spotlightEvent.shadow"
                this.isLocalizationKey = true
                this.location.set(screenY = 0.9f, screenHeight = 0.1f)
            }
            val colourPicker = ColourPicker(palette, contentStage, contentStage, hasAlpha = true).apply {
                this.setColor(this@SpotlightEvent.shadow)
                this.location.set(screenY = 0.4f, screenHeight = 0.5f)
                this.onColourChange = { c ->
                    this@SpotlightEvent.shadow.set(c)
                }
            }
            contentStage.elements += colourPicker

            this.updatePositions()
        }
    }
    
}