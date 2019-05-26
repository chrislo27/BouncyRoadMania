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
import io.github.chrislo27.bouncyroadmania.stage.ColourPicker
import io.github.chrislo27.bouncyroadmania.util.TrueCheckbox
import io.github.chrislo27.bouncyroadmania.util.fromJsonString
import io.github.chrislo27.bouncyroadmania.util.toJsonString
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


class DeployEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    companion object {
        val SEMITONE_RANGE = -12..12
    }

    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    override val shouldAlwaysBeSimulated: Boolean = true
    var firstBounceHasSound: Boolean = false
    val color: Color = Color(1f, 1f, 1f, 1f)
    var semitoneOffset: Int by Delegates.observable(0) { _, _, _ ->
        onParamsChange()
    }

    override val renderText: String
        get() = renderTextBacking
    private var renderTextBacking: String = ""

    init {
        this.bounds.width = 0.5f
        onParamsChange()
    }

    private fun onParamsChange() {
        renderTextBacking = (if (firstBounceHasSound) "*" else "") + super.renderText + (if (color.toFloatBits() != Color.WHITE_FLOAT_BITS) " ([#$color]█[])" else "") + (if (semitoneOffset > 0) " (+$semitoneOffset)" else if (semitoneOffset < 0) " ($semitoneOffset)" else "")
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
        engine.entities += Ball(engine, this.bounds.width, this.bounds.x, firstBounceHasSound, Color(color), semitoneOffset).apply {
            startOff()
        }
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        firstBounceHasSound = node["firstBounceHasSound"]?.asBoolean(false) ?: false
        color.fromJsonString(node["color"]?.asText())
        semitoneOffset = node["semitone"]?.asInt(0) ?: 0
        onParamsChange()
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        if (firstBounceHasSound) {
            node.put("firstBounceHasSound", firstBounceHasSound)
        }
        if (color != Color.WHITE) {
            node.put("color", color.toJsonString())
        }
        if (semitoneOffset != 0) {
            node.put("semitone", semitoneOffset)
        }
    }

    override fun copy(): DeployEvent {
        return DeployEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.firstBounceHasSound = this.firstBounceHasSound
            it.color.set(this.color)
            it.semitoneOffset = this.semitoneOffset
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
                            onParamsChange()
                        }

                        override fun undo(context: Editor) {
                            event.firstBounceHasSound = !value
                            checkbox.get()?.checked = !value
                            onParamsChange()
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
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "deployEvent.ballColour"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 2))
            }
            contentStage.elements += ColourPicker(palette, contentStage, contentStage, true).apply {
                this.setColor(color)
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size * 4, pixelY = -(size * 6))
                this.onColourChange = { c ->
                    color.set(c)
                    onParamsChange()
                }
            }

            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "deployEvent.semitone"
                this.location.set(screenY = 1f, screenHeight = 0f, screenWidth = 0.5f, pixelHeight = size, pixelY = -(size * 7))
            }
            contentStage.elements += object : TextLabel<EditorScreen>(palette, contentStage, contentStage) {
                override fun getRealText(): String {
                    return this@DeployEvent.semitoneOffset.toString()
                }
            }.apply {
                this.background = true
                this.isLocalizationKey = false
                this.location.set(screenY = 1f, screenHeight = 0f, screenX = 0.7f, screenWidth = 0.1f, pixelHeight = size, pixelY = -(size * 7f))
            }
            val buttonPalette = palette.copy(fontScale = 0.85f)
            contentStage.elements += Button(buttonPalette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = "-5"
                })
                this.leftClickAction = { _, _ ->
                    this@DeployEvent.semitoneOffset -= 5
                    this@DeployEvent.semitoneOffset = this@DeployEvent.semitoneOffset.coerceIn(SEMITONE_RANGE)
                }
                this.location.set(screenY = 1f, screenHeight = 0f, screenX = 0.5f + 0.025f / 2, screenWidth = 0.075f, pixelHeight = size * 0.75f, pixelY = -(size * 7f) + size * 0.125f)
            }
            contentStage.elements += Button(buttonPalette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = "-1"
                })
                this.leftClickAction = { _, _ ->
                    this@DeployEvent.semitoneOffset -= 1
                    this@DeployEvent.semitoneOffset = this@DeployEvent.semitoneOffset.coerceIn(SEMITONE_RANGE)
                }
                this.location.set(screenY = 1f, screenHeight = 0f, screenX = 0.6f + 0.025f / 2, screenWidth = 0.075f, pixelHeight = size * 0.75f, pixelY = -(size * 7f) + size * 0.125f)
            }
            contentStage.elements += Button(buttonPalette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = "+1"
                })
                this.leftClickAction = { _, _ ->
                    this@DeployEvent.semitoneOffset += 1
                    this@DeployEvent.semitoneOffset = this@DeployEvent.semitoneOffset.coerceIn(SEMITONE_RANGE)
                }
                this.location.set(screenY = 1f, screenHeight = 0f, screenX = 0.8f + 0.025f / 2, screenWidth = 0.075f, pixelHeight = size * 0.75f, pixelY = -(size * 7f) + size * 0.125f)
            }
            contentStage.elements += Button(buttonPalette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = "+5"
                })
                this.leftClickAction = { _, _ ->
                    this@DeployEvent.semitoneOffset += 5
                    this@DeployEvent.semitoneOffset = this@DeployEvent.semitoneOffset.coerceIn(SEMITONE_RANGE)
                }
                this.location.set(screenY = 1f, screenHeight = 0f, screenX = 0.9f + 0.025f / 2, screenWidth = 0.075f, pixelHeight = size * 0.75f, pixelY = -(size * 7f) + size * 0.125f)
            }
        }
    }

}