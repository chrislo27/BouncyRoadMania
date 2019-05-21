package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.GradientDirection
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.stage.ColourPicker
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class InitialParamsStage(val editor: Editor, parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    val engine: Engine get() = editor.engine

    val gradientFirst: ColourPicker<EditorScreen>
    val gradientLast: ColourPicker<EditorScreen>
    val gradientDir: Button<EditorScreen>
    val normalBouncerTint: ColourPicker<EditorScreen>
    val aBouncerTint: ColourPicker<EditorScreen>
    val dpadBouncerTint: ColourPicker<EditorScreen>

    init {
        val gradientPalette = palette.copy(fontScale = 0.75f)
        // Gradient settings
        elements += TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "initialParams.gradientFirst"
            this.fontScaleMultiplier = 0.85f
            this.background = true
            this.location.set(screenX = 0.05f, screenY = 0.95f, screenWidth = 0.2f, screenHeight = 0.05f)
        }
        gradientFirst = ColourPicker(gradientPalette, this, this).apply {
            this.location.set(screenX = 0.05f, screenY = 0.75f, screenWidth = 0.2f, screenHeight = 0.2f)
            this.onColourChange = { c ->
                engine.gradientStart.set(c)
            }
        }
        elements += gradientFirst
        elements += TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "initialParams.gradientLast"
            this.fontScaleMultiplier = 0.85f
            this.background = true
            this.location.set(screenX = 0.05f, screenY = 0.7f, screenWidth = 0.2f, screenHeight = 0.05f)
        }
        gradientLast = ColourPicker(gradientPalette, this, this).apply {
            this.location.set(screenX = 0.05f, screenY = 0.5f, screenWidth = 0.2f, screenHeight = 0.2f)
            this.onColourChange = { c ->
                engine.gradientEnd.set(c)
            }
        }
        elements += gradientLast
        elements += Button(palette, this, this).apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_swap"))
            })
            this.leftClickAction = { _, _ ->
                val tmp = Color(1f, 1f, 1f, 1f).set(engine.gradientStart)
                engine.gradientStart.set(engine.gradientEnd)
                engine.gradientEnd.set(tmp)
                updateColours()
            }
            this.location.set(screenX = 0.05f, screenY = 0.75f, screenWidth = 0f, screenHeight = 0f,
                    pixelX = -48f, pixelWidth = 48f, pixelY = -48f, pixelHeight = 48f)
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "initialParams.swap"
        }
        gradientDir = object : Button<EditorScreen>(palette, this, this) {
            val verticalLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_arrow_up"))
            }
            val horizontalLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_arrow_up"))
                this.rotation = 270f
            }

            fun updateDir() {
                removeLabel(verticalLabel)
                removeLabel(horizontalLabel)
                addLabel(if (engine.gradientDirection == GradientDirection.VERTICAL) verticalLabel else horizontalLabel)
                parent.updatePositions()
            }
        }.apply {
            this.leftClickAction = { _, _ ->
                engine.gradientDirection = if (engine.gradientDirection == GradientDirection.VERTICAL) GradientDirection.HORIZONTAL else GradientDirection.VERTICAL
                updateColours()
                updateDir()
            }
            updateDir()
            this.location.set(screenX = 0.05f, screenY = 0.75f, screenWidth = 0f, screenHeight = 0f,
                    pixelX = -48f, pixelWidth = 48f, pixelY = 0f, pixelHeight = 48f)
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "initialParams.changeDir"
        }
        elements += gradientDir

        // Bouncer tints
        elements += TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.fontScaleMultiplier = 0.85f
            this.text = "initialParams.normalBouncerTint"
            this.background = true
            this.location.set(screenX = 0.3f, screenY = 0.95f, screenWidth = 0.2f, screenHeight = 0.05f)
        }
        normalBouncerTint = ColourPicker(gradientPalette, this, this).apply {
            this.location.set(screenX = 0.3f, screenY = 0.75f, screenWidth = 0.2f, screenHeight = 0.2f)
            this.onColourChange = { c ->
                engine.normalBouncerTint.set(c)
            }
        }
        elements += normalBouncerTint
        elements += Button(palette, this, this).apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_back"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "initialParams.reset"
            this.leftClickAction = { _, _ ->
                normalBouncerTint.setColor(Engine.DEFAULT_NORMAL_BOUNCER)
            }
            this.location.set(screenX = 0.3f, screenY = 0.95f, screenWidth = 0f, screenHeight = 0.05f, pixelX = -32f, pixelWidth = 32f)
        }
        elements += TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.fontScaleMultiplier = 0.85f
            this.text = "initialParams.aBouncerTint"
            this.background = true
            this.location.set(screenX = 0.3f, screenY = 0.7f, screenWidth = 0.2f, screenHeight = 0.05f)
        }
        aBouncerTint = ColourPicker(gradientPalette, this, this).apply {
            this.location.set(screenX = 0.3f, screenY = 0.5f, screenWidth = 0.2f, screenHeight = 0.2f)
            this.onColourChange = { c ->
                engine.aBouncerTint.set(c)
            }
        }
        elements += aBouncerTint
        elements += Button(palette, this, this).apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_back"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "initialParams.reset"
            this.leftClickAction = { _, _ ->
                aBouncerTint.setColor(Engine.DEFAULT_A_BOUNCER)
            }
            this.location.set(screenX = 0.3f, screenY = 0.7f, screenWidth = 0f, screenHeight = 0.05f, pixelX = -32f, pixelWidth = 32f)
        }
        elements += TextLabel(palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = true
            this.fontScaleMultiplier = 0.85f
            this.text = "initialParams.dpadBouncerTint"
            this.background = true
            this.location.set(screenX = 0.3f, screenY = 0.45f, screenWidth = 0.2f, screenHeight = 0.05f)
        }
        dpadBouncerTint = ColourPicker(gradientPalette, this, this).apply {
            this.location.set(screenX = 0.3f, screenY = 0.25f, screenWidth = 0.2f, screenHeight = 0.2f)
            this.onColourChange = { c ->
                engine.dpadBouncerTint.set(c)
            }
        }
        elements += dpadBouncerTint
        elements += Button(palette, this, this).apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_back"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "initialParams.reset"
            this.leftClickAction = { _, _ ->
                dpadBouncerTint.setColor(Engine.DEFAULT_DPAD_BOUNCER)
            }
            this.location.set(screenX = 0.3f, screenY = 0.45f, screenWidth = 0f, screenHeight = 0.05f, pixelX = -32f, pixelWidth = 32f)
        }

        updateColours()
    }

    fun updateColours(engine: Engine = this.engine) {
        gradientFirst.setColor(engine.gradientStart)
        gradientLast.setColor(engine.gradientEnd)
        normalBouncerTint.setColor(engine.normalBouncerTint)
        aBouncerTint.setColor(engine.aBouncerTint)
        dpadBouncerTint.setColor(engine.dpadBouncerTint)
    }

    fun onEngineChange(engine: Engine) {
        updateColours(engine)
    }

}