package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.saveTo
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


class SaveButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    @Volatile
    var isOpen: Boolean = false
        private set

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (!isOpen) {
            isOpen = true
            Gdx.app.postRunnable {
                editorStage.elements += MenuOverlay(editor, editorStage, palette).apply overlay@{
                    val label = TextLabel(palette, this, this).apply {
                        background = true
                        isLocalizationKey = false
                        text = Localization["closeChooser"]
                    }
                    elements += label

                    GlobalScope.launch {
                        val lastSaveFile = editor.lastSaveFile
                        val filter = TinyFDWrapper.Filter(listOf("*.${BRMania.FILE_EXTENSION}"), Localization["save.fileFilter"] + " (.${BRMania.FILE_EXTENSION})")
                        val file = TinyFDWrapper.saveFile(Localization["save.fileChooserTitle"], lastSaveFile?.file()?.absolutePath ?: attemptRememberDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_SAVE)?.absolutePath?.plus("/"), filter)
                        if (file != null) {
                            try {
                                Gdx.app.postRunnable {
                                    label.text = Localization["save.saving"]
                                }
                                val correctFile = if (file.extension != BRMania.FILE_EXTENSION)
                                    file.parentFile.resolve("${file.name}.${BRMania.FILE_EXTENSION}")
                                else
                                    file

                                val engine = editor.engine
                                engine.saveTo(correctFile, false)
                                editor.setFileHandles(FileHandle(correctFile))
                                Gdx.app.postRunnable {
                                    this@overlay.removeSelf()
                                    isOpen = false
                                }
                                persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_SAVE, correctFile.parentFile)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Gdx.app.postRunnable {
                                    label.text = Localization["save.failed", e::class.java.canonicalName]
                                    this@overlay.elements += Button(palette, this@overlay, this@overlay).apply {
                                        this.addLabel(TextLabel(palette, this, this.stage).apply {
                                            this.isLocalizationKey = true
                                            this.textWrapping = false
                                            this.text = "continue"
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