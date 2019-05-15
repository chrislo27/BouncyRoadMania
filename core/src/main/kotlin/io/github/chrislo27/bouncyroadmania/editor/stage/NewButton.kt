package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.stage.GenericStage
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class NewButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {
    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        Gdx.app.postRunnable {
            editorStage.elements += MenuOverlay(editor, editorStage, palette).apply {
                elements += NewEngineStage(editor, palette, this)
            }
            editorStage.updatePositions()
        }
    }
}

class NewEngineStage(val editor: Editor, palette: UIPalette, parent: MenuOverlay)
    : GenericStage<EditorScreen>(palette, parent, parent.camera) {

    init {
        onBackButtonClick = {
            backButton.enabled = false
            Gdx.app.postRunnable {
                parent.removeSelf()
            }
        }
        backButton.visible = true
        titleLabel.text = "newEngine.title"
        titleLabel.isLocalizationKey = true
        titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_new_engine"))
        
        bottomStage.elements += Button(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                        clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)),
                        bottomStage, bottomStage).apply {
                    this.location.set(screenX = 0.25f, screenWidth = 0.5f)
                    this.addLabel(TextLabel(palette, this, this.stage).apply {
                        this.textAlign = Align.center
                        this.text = "newEngine.button"
                        this.isLocalizationKey = true
                    })
                    this.leftClickAction = { _, _ ->
                        editor.engine = Engine()
                        Gdx.app.postRunnable {
                            parent.removeSelf()
                            System.gc()
                        }
                    }
                }
        centreStage.elements += TextLabel(palette, centreStage, centreStage).apply {
            this.location.set(screenX = 0.5f, screenWidth = 0.5f - 0.125f)
            this.textAlign = Align.left
            this.isLocalizationKey = true
            this.text = "newEngine.warning"
        }
        val warn = ImageLabel(palette, centreStage, centreStage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_warning"))
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenY = 0.125f, screenHeight = 0.75f)
        }
        centreStage.elements += warn
        warn.apply {
            stage.updatePositions()
            this.location.set(screenWidth = stage.percentageOfWidth(this.location.realHeight))
            this.location.set(screenX = 0.5f - this.location.screenWidth)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE && backButton.enabled) {
            onBackButtonClick()
            return true
        }

        return false
    }
}
