package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class ToolbarStage(parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
        }
        val editor = parent.editor

        val buttonSize = 32f
        val buttonPadding = 6f

        // I/O buttons
        this.elements += Button(palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_new_button"))
            })
            this.tooltipText = "editor.new"
            this.tooltipTextIsLocalizationKey = true
        }
        this.elements += Button(palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 2 + buttonSize,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_load_button"))
            })
            this.tooltipText = "editor.load"
            this.tooltipTextIsLocalizationKey = true
        }
        this.elements += Button(palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 3 + buttonSize * 2,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_save_button"))
            })
            this.tooltipText = "editor.save"
            this.tooltipTextIsLocalizationKey = true
        }

        // Separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, buttonPadding * 3 + buttonSize * 2 + buttonSize + buttonPadding * 0.5f - 1f, 2f, 2f, 36f)
        }

        this.elements += MusicButton(editor, palette, this, parent).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 4 + buttonSize * 3,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
        }
        this.elements += MetronomeButton(editor, palette, this, parent).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = buttonPadding * 5 + buttonSize * 4,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
        }

        // Separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, 640f - buttonSize / 2f - buttonSize - buttonPadding * 2f + 1f, 2f, 2f, 36f)
        }

        this.elements += PlaybackButton(PlayState.PLAYING, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_play"))
                this.tint = Color.valueOf("00A810")
            })
        }
        this.elements += PlaybackButton(PlayState.PAUSED, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f - buttonSize - buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_pause"))
                this.tint = Color.valueOf("E8D140")
            })
        }
        this.elements += PlaybackButton(PlayState.STOPPED, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize + buttonPadding,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_stop"))
                this.tint = Color.valueOf("ED0000")
            })
        }

        // Separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, 640f - buttonSize / 2f + buttonSize * 2 + buttonPadding * 1.5f - 1f, 2f, 2f, 36f)
        }

        this.elements += EditModeButton(EditMode.ENGINE, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 2 + buttonPadding * 2,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_engine"))
            })
        }
        this.elements += EditModeButton(EditMode.EVENTS, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 3 + buttonPadding * 3,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_events"))
            })
        }
        this.elements += EditModeButton(EditMode.PARAMETERS, editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 4 + buttonPadding * 4,
                    pixelWidth = buttonSize, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_edit_mode_params"))
            })
        }
        // Separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 0f, 0f, 640f - buttonSize / 2f + buttonSize * 5 + buttonPadding * 4.5f - 1f, 2f, 2f, 36f)
        }

        this.elements += SnapButton(editor, palette, this, this).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelX = 640f - buttonSize / 2f + buttonSize * 5 + buttonPadding * 5,
                    pixelWidth = buttonSize * 4f, pixelHeight = buttonSize, pixelY = -(4f + buttonSize))
        }

        // Bottom separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 1f, 0f, pixelHeight = 1f)
        }
    }

}