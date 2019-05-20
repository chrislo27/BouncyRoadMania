package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.screen.MainMenuScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*


class ExitButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editorStage.elements += MenuOverlay(editor, editorStage, palette).apply overlay@{
            elements += ColourPane(this, this).apply {
                this.colour.set(0f, 0f, 0f, 0.75f)
            }
            elements += Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
                elements += TextLabel(editor.stage.palette, this, this).apply {
                    this.textWrapping = false
                    this.isLocalizationKey = false
                    this.text = Localization["editor.quit.confirmation"]
                }
                elements += Button(editor.stage.palette.copy(highlightedBackColor = Color(0f, 1f, 0f, 0.5f), clickedBackColor = Color(0.5f, 1f, 0.5f, 0.5f)), this, this).apply {
                    this.addLabel(TextLabel(palette, this, this.stage).apply {
                        this.isLocalizationKey = true
                        this.textWrapping = false
                        this.text = "yes"
                    })
                    this.leftClickAction = { _, _ ->
                        Gdx.app.postRunnable {
                            this@overlay.removeSelf()
                            editor.main.screen = MainMenuScreen(editor.main)
                        }
                    }
                    this.location.set(screenX = 0.55f, screenWidth = 0.35f, screenY = 0.05f, screenHeight = 0.15f)
                }
                elements += Button(editor.stage.palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                        clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), this, this).apply {
                    this.addLabel(TextLabel(palette, this, this.stage).apply {
                        this.isLocalizationKey = true
                        this.textWrapping = false
                        this.text = "cancel"
                    })
                    this.leftClickAction = { _, _ ->
                        Gdx.app.postRunnable {
                            this@overlay.removeSelf()
                        }
                    }
                    this.location.set(screenX = 0.1f, screenWidth = 0.35f, screenY = 0.05f, screenHeight = 0.15f)
                }

                updatePositions()
            }
        }
        editorStage.updatePositions()
    }
}