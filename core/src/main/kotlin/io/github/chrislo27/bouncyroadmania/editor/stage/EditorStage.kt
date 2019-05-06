package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
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
        this.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
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
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_load_button"))
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

        // Separator
        toolbarStage.elements += ColourPane(toolbarStage, toolbarStage).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, 640f - buttonSize / 2f - buttonSize - buttonPadding * 2f + 1f, 2f, 2f, 36f)
        }

        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_play"))
            })
            this.tooltipText = "Play"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f - buttonSize - buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_pause"))
            })
            this.tooltipText = "Pause"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize + buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_stop"))
            })
            this.tooltipText = "Stop"
        }

        // Separator
        toolbarStage.elements += ColourPane(toolbarStage, toolbarStage).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, 640f - buttonSize / 2f + buttonSize * 2 + buttonPadding * 2 - 1f, 2f, 2f, 36f)
        }

        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 2 + buttonPadding * 3,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_engine"))
            })
            this.tooltipText = "Edit Mode: Engine"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 3 + buttonPadding * 4,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_events"))
            })
            this.tooltipText = "Edit Mode: Events"
        }
        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 4 + buttonPadding * 5,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_params"))
            })
            this.tooltipText = "Edit Mode: Parameters"
        }

        toolbarStage.elements += Button(palette, toolbarStage, toolbarStage).apply {
            this.location.set(1f, 1f, 0f, 0f, pixelX = -(buttonPadding + buttonSize),
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_save_button"))
            })
            this.tooltipText = "Test Right Edge"
        }
    }

}