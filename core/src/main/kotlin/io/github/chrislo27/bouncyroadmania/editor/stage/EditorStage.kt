package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class EditorStage(val editor: Editor)
    : Stage<EditorScreen>(null, OrthographicCamera().apply { setToOrtho(false, 1280f, 720f) }, 1280f, 720f) {

    val toolbarStage: Stage<EditorScreen>

    init {
        val palette = BRManiaApp.instance.uiPalette
        toolbarStage = Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
            this.location.set(screenHeight = 0f, screenY = 1f, pixelY = -40f, pixelHeight = 40f)
            elements += ColourPane(this, this).apply {
                this.colour.set(0f, 0f, 0f, 0.5f)
            }
        }
        elements += toolbarStage
        this.tooltipElement = TextLabel(palette.copy(backColor = palette.backColor.cpy().also { it.a = (it.a * 1.5f).coerceIn(0f, 1f) }), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
            this.fontScaleMultiplier = 0.75f
        }

        val buttonSize = 32f
        val buttonPadding = 6f
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_new_button"))
            })
            this.tooltipText = "New"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 2 + buttonSize,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_folder"))
            })
            this.tooltipText = "Open"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 3 + buttonSize * 2,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_save_button"))
            })
            this.tooltipText = "Save"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(1f, 1f, 0f, 0f, pixelX = -(buttonPadding + buttonSize),
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_save_button"))
            })
            this.tooltipText = "Test"
        }
    }

}