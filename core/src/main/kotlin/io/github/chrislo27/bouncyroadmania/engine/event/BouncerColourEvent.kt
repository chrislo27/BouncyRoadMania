package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.stage.ColourPicker
import io.github.chrislo27.bouncyroadmania.util.fromJsonString
import io.github.chrislo27.bouncyroadmania.util.toJsonString
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import java.lang.ref.WeakReference


class BouncerColourEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    enum class BouncerType(val icon: String, val textureKey: String, val localizationKey: String) {
        NORMAL("", "tex_bouncer_blue", "bouncerColourEvent.normalBouncer"),
        A("\uE0E0", "tex_bouncer_yellow", "bouncerColourEvent.aBouncer"),
        DPAD("\uE110", "tex_bouncer_red", "bouncerColourEvent.dpadBouncer");
        companion object {
            val VALUES = values().toList()
        }
    }

    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    override val shouldAlwaysBeSimulated: Boolean = true

    val color: Color = Color(1f, 1f, 1f, 1f)
    var bouncerType: BouncerType = BouncerType.NORMAL
    private val tintTarget: Color get() = when (bouncerType) {
        BouncerType.NORMAL -> engine.normalBouncerCurrentTint
        BouncerType.A -> engine.aBouncerCurrentTint
        BouncerType.DPAD -> engine.dpadBouncerCurrentTint
    }
    val tintOrigin: Color get() = when (bouncerType) {
        BouncerType.NORMAL -> engine.normalBouncerTint
        BouncerType.A -> engine.aBouncerTint
        BouncerType.DPAD -> engine.dpadBouncerTint
    }

    override val renderText: String
        get() = renderTextBacking
    private var renderTextBacking: String = ""

    private val startColor = Color(1f, 1f, 1f, 1f)

    init {
        onColorChange()
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): BouncerColourParamsStage {
        return BouncerColourParamsStage(stage)
    }

    fun onColorChange() {
        renderTextBacking = super.renderText + " ${bouncerType.icon} [#$color]█[]"
    }

    override fun copy(): BouncerColourEvent {
        return BouncerColourEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.color.set(this.color)
            it.bouncerType = this.bouncerType
        }
    }

    override fun onStart() {
        startColor.set(tintTarget)
    }

    override fun whilePlaying() {
        val a = ((engine.beat - bounds.x) / bounds.width).coerceIn(0f, 1f)
        tintTarget.set(startColor).lerp(color, a)
    }

    override fun onEnd() {
        tintTarget.set(color)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        color.fromJsonString(node["color"]?.asText())
        val t = node["bouncerType"]?.asText() ?: "???"
        bouncerType = BouncerType.VALUES.find { it.name == t } ?: BouncerType.NORMAL
        onColorChange()
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        node.put("color", color.toJsonString())
        node.put("bouncerType", bouncerType.name)
    }

    inner class BouncerColourParamsStage(parent: EditorStage) : EventParamsStage<BouncerColourEvent>(parent, this@BouncerColourEvent) {
        init {
            val colourPicker = ColourPicker(palette, contentStage, contentStage).apply {
                this.setColor(this@BouncerColourEvent.color)
                this.location.set(screenY = 0.5f, screenHeight = 0.5f)
                this.onColourChange = { c ->
                    this@BouncerColourEvent.color.set(c)
                    this@BouncerColourEvent.onColorChange()
                }
            }
            contentStage.elements += colourPicker

            contentStage.elements += Button(palette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.textWrapping = false
                    this.isLocalizationKey = true
                    this.text = "bouncerColourEvent.resetToMostRecent"
                })
                this.leftClickAction = { _, _ ->
                    val mostRecentColor = engine.events.sortedBy { it.bounds.maxX }.asReversed()
                            .filterIsInstance<BouncerColourEvent>()
                            .find { it.bouncerType == bouncerType && it.bounds.maxX < bounds.maxX && it != this@BouncerColourEvent }
                    val c = Color(mostRecentColor?.color ?: tintOrigin)
                    parent.editor.mutate(object : ReversibleAction<Editor> {
                        val picker: WeakReference<ColourPicker<EditorScreen>> = WeakReference(colourPicker)
                        val lastColor = Color(this@BouncerColourEvent.color)
                        override fun redo(context: Editor) {
                            this@BouncerColourEvent.color.set(c)
                            this@BouncerColourEvent.onColorChange()
                            picker.get()?.setColor(c, false)
                        }

                        override fun undo(context: Editor) {
                            this@BouncerColourEvent.color.set(lastColor)
                            this@BouncerColourEvent.onColorChange()
                            picker.get()?.setColor(lastColor, false)
                        }
                    })
                }
                this.location.set(screenY = 0.175f, screenHeight = 0.15f)
            }

            contentStage.elements += Button(palette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.textWrapping = false
                    this.isLocalizationKey = true
                    this.text = "bouncerColourEvent.resetToInitial"
                })
                this.leftClickAction = { _, _ ->
                    val c = Color(tintOrigin)
                    parent.editor.mutate(object : ReversibleAction<Editor> {
                        val picker: WeakReference<ColourPicker<EditorScreen>> = WeakReference(colourPicker)
                        val lastColor = Color(this@BouncerColourEvent.color)
                        override fun redo(context: Editor) {
                            this@BouncerColourEvent.color.set(c)
                            this@BouncerColourEvent.onColorChange()
                            picker.get()?.setColor(c, false)
                        }

                        override fun undo(context: Editor) {
                            this@BouncerColourEvent.color.set(lastColor)
                            this@BouncerColourEvent.onColorChange()
                            picker.get()?.setColor(lastColor, false)
                        }
                    })
                }
                this.location.set(screenY = 0f, screenHeight = 0.15f)
            }
            
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "bouncerColourEvent.bouncerType"
                this.textWrapping = false
                this.location.set(screenY = 0.35f, screenHeight = 0.125f, screenWidth = 0.5f, pixelWidth = -4f)
            }
            contentStage.elements += Button(palette, contentStage, contentStage).apply {
                this.location.set(screenY = 0.35f, screenHeight = 0.125f, screenWidth = 0.5f, screenX = 0.5f)
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = true
                    this.text = bouncerType.localizationKey
                    this.textWrapping = false
                    this.fontScaleMultiplier = 0.85f
                })
                fun cycle(dir: Int) {
                    val current = bouncerType
                    val values = BouncerType.VALUES
                    val nextIndex = values.indexOf(current) + dir.coerceIn(-1, 1)
                    val next: BouncerType = if (nextIndex < 0) values.last() else if (nextIndex >= values.size) values.first() else values[nextIndex]
                    bouncerType = next
                    (labels.first() as TextLabel).text = next.localizationKey
                    onColorChange()
                }
                this.leftClickAction = { _, _ ->
                    cycle(1)
                }
                this.rightClickAction = { _, _ ->
                    cycle(-1)
                }
            }
        }
    }

}