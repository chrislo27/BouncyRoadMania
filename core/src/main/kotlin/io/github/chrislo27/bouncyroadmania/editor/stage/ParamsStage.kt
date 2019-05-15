package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.bouncyroadmania.editor.CameraPan
import io.github.chrislo27.bouncyroadmania.editor.oopsies.impl.EventSelectionAction
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.engine.event.InstantiatedEvent
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


@Suppress("LeakingThis")
open class ParamsStage(parent: EditorStage)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    // y: 192  h: 448

    val palette: UIPalette = parent.palette
    val title: TextLabel<EditorScreen>
    val contentStage: Stage<EditorScreen>
    open val mustCloseWhenPlaying: Boolean = true

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.8f)
        }
        elements += Button(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f), clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), this, this).apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_x"))
            })
            this.location.set(screenX = 1f, screenY = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = -32f, pixelY = -32f, pixelWidth = 32f, pixelHeight = 32f)
            this.leftClickAction = { _, _ ->
                Gdx.app.postRunnable {
                    parent.removeChild(this@ParamsStage)
                }
            }
            tooltipTextIsLocalizationKey = true
            tooltipText = "editor.params.close"
        }
        title = TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.location.set(screenX = 0f, screenWidth = 1f, screenY = 1f, screenHeight = 0f, pixelX = 4f + 32f, pixelWidth = -8f - (32f * 2), pixelHeight = 32f, pixelY = -32f)
        }
        elements += title
        contentStage = Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
            this.location.set(screenX = 0f, screenY = 0f, screenWidth = 1f, screenHeight = 1f, pixelX = 8f, pixelY = 8f, pixelWidth = -16f, pixelHeight = -16f - 32f)
        }
        elements += contentStage
        updatePositions()
    }
}

@Suppress("LeakingThis")
open class EventParamsStage<E : Event>(parent: EditorStage, val event: E) : ParamsStage(parent) {
    
    final override val mustCloseWhenPlaying: Boolean = true
    
    val findButton: Button<EditorScreen>

    init {
        if (event is InstantiatedEvent) {
            title.isLocalizationKey = event.instantiator.nameIsLocalizationKey
            title.text = event.instantiator.name
        }

        findButton = Button(palette, this, this).apply {
            this.location.set(screenY = 1f, screenWidth = 0f, screenHeight = 0f, pixelY = -32f, pixelHeight = 32f, pixelWidth = 32f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_magnifying_glass"))
            })
            this.leftClickAction = { _, _ ->
                val editor = parent.editor
                val renderer = editor.renderer
                renderer.cameraPan = CameraPan(renderer.trackCamera.position.x, event.bounds.x + event.bounds.width / 2, 0.25f, Interpolation.exp10Out)
                editor.mutate(EventSelectionAction(editor.selection.toList(), listOf(event)))
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "editor.params.find"
        }
        elements += findButton
    }
}