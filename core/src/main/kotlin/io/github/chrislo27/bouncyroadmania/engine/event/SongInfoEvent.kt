package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.util.TrueCheckbox
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import java.lang.ref.WeakReference
import kotlin.math.min
import kotlin.properties.Delegates


class SongInfoEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val isUnique: Boolean = true
    override val canBeCopied: Boolean = true
    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true

    var staticMode: Boolean = false
    var concatenated: String = ""
        private set
    var songTitle: String by Delegates.observable("") { _, _, new ->
        concatenated = if (songArtist.isEmpty()) new else "$new - $songArtist"
    }
    var songArtist: String by Delegates.observable("") { _, _, new ->
        concatenated = if (songTitle.isEmpty()) new else "$songTitle - $new"
    }

    override fun copy(): SongInfoEvent {
        return SongInfoEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
        }
    }

    fun getProgress(): Float {
        val secondsStart = engine.tempos.beatsToSeconds(bounds.x)
        val secondsDuration = engine.tempos.beatsToSeconds(bounds.maxX) - secondsStart
        val transitionTime = min(secondsDuration / 2f, 0.5f)
        val alpha = when (val s = (engine.seconds - secondsStart).coerceIn(0f, secondsDuration)) {
            in 0f..transitionTime -> s / transitionTime
            in secondsDuration - transitionTime..secondsDuration -> 1f - (s - (secondsDuration - transitionTime)) / transitionTime
            else -> 1f
        }
        return Interpolation.circle.apply(alpha)
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): SongInfoEventParamsStage {
        return SongInfoEventParamsStage(stage)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        staticMode = node["staticMode"]?.asBoolean(false) ?: false
        songTitle = node["songTitle"]?.asText(null) ?: "???"
        songArtist = node["songArtist"]?.asText(null) ?: "???"
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        node.put("staticMode", staticMode)
        node.put("songTitle", songTitle)
        node.put("songArtist", songArtist)
    }

    inner class SongInfoEventParamsStage(parent: EditorStage) : EventParamsStage<SongInfoEvent>(parent, this@SongInfoEvent) {

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
                            event.staticMode = value
                            checkbox.get()?.checked = value
                        }

                        override fun undo(context: Editor) {
                            event.staticMode = !value
                            checkbox.get()?.checked = !value
                        }
                    })
                }
            }.apply {
                this.checked = event.staticMode
                this.textLabel.isLocalizationKey = true
                this.textLabel.text = "songInfoEvent.static"
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "songInfoEvent.static.tooltip"
                this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -size, pixelHeight = size)
            }

            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "songInfoEvent.songTitle"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 2))
            }
            contentStage.elements += object : TextField<EditorScreen>(palette, contentStage, contentStage) {
                override fun onEnterPressed(): Boolean {
                    hasFocus = false
                    event.songTitle = text.trim()
                    return true
                }
            }.apply {
                this.background = true
                this.canInputNewlines = true
                this.canPaste = true
                this.textAlign = Align.left
                this.text = event.songTitle
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 3))
            }
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "songInfoEvent.songArtist"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 4))
            }
            contentStage.elements += object : TextField<EditorScreen>(palette, contentStage, contentStage) {
                override fun onEnterPressed(): Boolean {
                    hasFocus = false
                    event.songArtist = text.trim()
                    return true
                }
            }.apply {
                this.background = true
                this.canInputNewlines = true
                this.canPaste = true
                this.textAlign = Align.left
                this.text = event.songArtist
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 5))
            }
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.isLocalizationKey = true
                this.text = "textField.pressEnterToFinish"
                this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = size, pixelY = -(size * 5.75f))
                this.fontScaleMultiplier = 0.75f
            }

            this.updatePositions()
        }
    }

}