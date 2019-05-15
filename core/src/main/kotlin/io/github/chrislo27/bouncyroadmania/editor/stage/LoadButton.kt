package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.unpack
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.util.TinyFDWrapper
import io.github.chrislo27.bouncyroadmania.util.attemptRememberDirectory
import io.github.chrislo27.bouncyroadmania.util.persistDirectory
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile


class LoadButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    @Volatile
    var isOpen: Boolean = false
        private set

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        load(null)
    }

    @Synchronized
    fun load(initialFile: File?) {
        if (!isOpen) {
            isOpen = true
            Gdx.app.postRunnable {
                editorStage.elements += MenuOverlay(editor, editorStage, palette).apply overlay@{
                    val label = TextLabel(palette, this, this).apply {
                        background = true
                        isLocalizationKey = false
                        text = Localization[if (initialFile == null) "closeChooser" else "load.loading"]
                    }
                    elements += label

                    GlobalScope.launch {
                        val filter = TinyFDWrapper.Filter(listOf("*.${BRMania.FILE_EXTENSION}"), Localization["save.fileFilter"] + " (.${BRMania.FILE_EXTENSION})")
                        val file = initialFile ?: TinyFDWrapper.openFile(Localization["load.fileChooserTitle"], attemptRememberDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_LOAD)?.absolutePath?.plus("/"), false, filter)
                        if (file != null && file.exists()) {
                            try {
                                Gdx.app.postRunnable {
                                    label.text = Localization["load.loading"]
                                }
                                val zipFile = ZipFile(file)
                                val newEngine = Engine()
                                newEngine.unpack(zipFile)
                                editor.engine = newEngine
                                Gdx.app.postRunnable {
                                    this@overlay.removeSelf()
                                    isOpen = false
                                }
                                persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_LOAD, file.parentFile)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Gdx.app.postRunnable {
                                    label.text = Localization["load.failed", e::class.java.canonicalName]
                                    this@overlay.elements += Button(palette, this@overlay, this@overlay).apply {
                                        this.addLabel(TextLabel(palette, this, this.stage).apply {
                                            this.isLocalizationKey = true
                                            this.textWrapping = false
                                            this.text = "save.continue"
                                        })
                                        this.leftClickAction = { _, _ ->
                                            Gdx.app.postRunnable {
                                                this@overlay.removeSelf()
                                                isOpen = false
                                            }
                                        }
                                        this.location.set(screenX = 0.1f, screenWidth = 0.8f, screenY = 0.05f, screenHeight = 0.15f)
                                    }
                                    this@overlay.updatePositions()
                                }
                            }
                        } else {
                            Gdx.app.postRunnable {
                                this@overlay.removeSelf()
                                isOpen = false
                            }
                        }
                    }
                }
                editorStage.updatePositions()
            }
        }
    }

}

class LoadDragAndDropStage(parent: MenuOverlay, val editor: Editor, val file: File)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {
    init {
        elements += TextLabel(editor.stage.palette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = Localization["load.dragAndDrop.confirmation", file.name]
        }
        elements += Button(editor.stage.palette.copy(highlightedBackColor = Color(0f, 1f, 0f, 0.5f), clickedBackColor = Color(0.5f, 1f, 0.5f, 0.5f)), this, this).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "yes"
            })
            this.leftClickAction = { _, _ ->
                Gdx.app.postRunnable {
                    parent.removeSelf()
                    editor.stage.toolbarStage.loadButton.load(file)
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
                    parent.removeSelf()
                }
            }
            this.location.set(screenX = 0.1f, screenWidth = 0.35f, screenY = 0.05f, screenHeight = 0.15f)
        }

        updatePositions()
    }
}