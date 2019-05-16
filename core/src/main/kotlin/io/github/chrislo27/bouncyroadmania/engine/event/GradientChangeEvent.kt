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


class GradientChangeEvent(engine: Engine, instantiator: Instantiator, val first: Boolean)
    : InstantiatedEvent(engine, instantiator) {

    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    override val shouldAlwaysBeSimulated: Boolean = true

    private val gradientTarget: Color get() = if (first) this.engine.gradientCurrentStart else this.engine.gradientCurrentEnd
    val color: Color = Color(1f, 1f, 1f, 1f)

    override val renderText: String
        get() = renderTextBacking
    private var renderTextBacking: String = ""

    private val startColor = Color(1f, 1f, 1f, 1f)

    init {
        onColorChange()
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): GradientChangeParamsStage {
        return GradientChangeParamsStage(stage)
    }

    fun onColorChange() {
        renderTextBacking = super.renderText + " [#$color]█[]"
    }

    override fun onStart() {
        startColor.set(gradientTarget)
    }

    override fun whilePlaying() {
        val a = ((engine.beat - bounds.x) / bounds.height).coerceIn(0f, 1f)
        gradientTarget.set(startColor).lerp(color, a)
    }

    override fun onEnd() {
        gradientTarget.set(color)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        color.fromJsonString(node["color"]?.asText())
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        node.put("color", color.toJsonString())
    }

    override fun copy(): GradientChangeEvent {
        return GradientChangeEvent(engine, instantiator, first).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.color.set(this.color)
        }
    }

    inner class GradientChangeParamsStage(parent: EditorStage) : EventParamsStage<GradientChangeEvent>(parent, this@GradientChangeEvent) {
        init {
            contentStage.elements += ColourPicker(palette, contentStage, contentStage).apply {
                this.setColor(this@GradientChangeEvent.color)
                this.location.set(screenY = 0.5f, screenHeight = 0.5f)
                this.onColourChange = { c ->
                    this@GradientChangeEvent.color.set(c)
                    this@GradientChangeEvent.onColorChange()
                }
            }
        }
    }
}