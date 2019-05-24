package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.engine.MusicData
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.MusicLoadingException
import io.github.chrislo27.bouncyroadmania.stage.GenericStage
import io.github.chrislo27.bouncyroadmania.util.TinyFDWrapper
import io.github.chrislo27.bouncyroadmania.util.attemptRememberDirectory
import io.github.chrislo27.bouncyroadmania.util.persistDirectory
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class MusicButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    private var wasMuted: Boolean? = null

    private val icons: List<TextureRegion> = listOf(
            TextureRegion(AssetRegistry.get<Texture>("ui_music")),
            TextureRegion(AssetRegistry.get<Texture>("ui_music_muted"))
    )
    private val label = ImageLabel(palette, this, stage)

    init {
        addLabel(label)
        tooltipText = "editor.music.${if (editor.engine.isMusicMuted) "unmute" else "mute"}"
        tooltipTextIsLocalizationKey = true
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val current = editor.engine.isMusicMuted
        if (wasMuted != current) {
            wasMuted = current

            label.image = icons[if (current) 1 else 0]
        }

        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        Gdx.app.postRunnable {
            editorStage.elements += MenuOverlay(editor, editorStage, palette).apply {
                elements += MusicSelectStage(editor, palette, this)
            }
            editorStage.updatePositions()
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        editor.engine.isMusicMuted = !editor.engine.isMusicMuted
        tooltipText = "editor.music.${if (editor.engine.isMusicMuted) "unmute" else "mute"}"
    }
}

class MusicSelectStage(val editor: Editor, palette: UIPalette, parent: MenuOverlay)
    : GenericStage<EditorScreen>(palette, parent, parent.camera) {

    val label: TextLabel<EditorScreen>
    val closeLabel: TextLabel<EditorScreen>
    val moveMusicStartButton: Button<EditorScreen>
    val selectButton: Button<EditorScreen>
    val removeButton: Button<EditorScreen>

    @Volatile
    var isChooserOpen = false
        private set(value) {
            field = value
            closeLabel.visible = value
            selectButton.enabled = !value
            removeButton.enabled = !value
            backButton.enabled = !value
            moveMusicStartButton.enabled = !value && editor.engine.music != null && editor.engine.music?.music?.getStartOfSound() != -editor.engine.musicStartSec
        }
    @Volatile
    var isLoading = false
        private set(value) {
            field = value
            selectButton.enabled = !value
            removeButton.enabled = !value
            backButton.enabled = !value
            moveMusicStartButton.enabled = !value && editor.engine.music != null && editor.engine.music?.music?.getStartOfSound() != -editor.engine.musicStartSec
        }

    init {
        onBackButtonClick = {
            backButton.enabled = false
            Gdx.app.postRunnable {
                parent.removeSelf()
            }
        }
        backButton.visible = true
        titleLabel.text = "musicSelect.title"
        titleLabel.isLocalizationKey = true
        titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_song_choose"))
        label = TextLabel(palette, centreStage, centreStage).apply {
            this.isLocalizationKey = false
            this.text = ""
        }
        centreStage.elements += label

        closeLabel = TextLabel(palette, centreStage, centreStage).apply {
            this.text = "closeChooser"
            this.isLocalizationKey = true
            this.location.set(screenHeight = 0.1f)
            this.visible = false
        }
        centreStage.elements += closeLabel

        removeButton = Button(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), bottomStage, bottomStage).apply {
            val backBtnLoc = this@MusicSelectStage.backButton.location
            this.location.set(1f - backBtnLoc.screenX - backBtnLoc.screenWidth, backBtnLoc.screenY, backBtnLoc.screenWidth, backBtnLoc.screenHeight)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_x"))
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "musicSelect.removeMusic"
        }
        bottomStage.elements += removeButton
        moveMusicStartButton = Button(palette, bottomStage, bottomStage).apply {
            this.location.set(removeButton.location)
            this.location.set(screenX = this.location.screenX - this.location.screenWidth * 1.75f, screenWidth = this.location.screenWidth * 1.65f)
            val mstartColor = editor.theme.trackers.musicStart.toString()
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.textWrapping = false
                this.text = Localization["musicSelect.moveMusicStart", mstartColor]
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 0.85f
            })
            this.tooltipTextIsLocalizationKey = false
            this.tooltipText = Localization["musicSelect.moveMusicStart.tooltip", mstartColor]
            this.leftClickAction = { _, _ ->
                val music = editor.engine.music
                if (music != null) {
                    editor.mutate(object : ReversibleAction<Editor> {
                        val old = editor.engine.musicStartSec
                        override fun redo(context: Editor) {
                            context.engine.musicStartSec = -music.music.getStartOfSound()
                        }

                        override fun undo(context: Editor) {
                            context.engine.musicStartSec = old
                        }
                    })
                    this@apply.enabled = false
                }
            }
        }
        bottomStage.elements += moveMusicStartButton
        selectButton = Button(palette, bottomStage, bottomStage).apply {
            leftClickAction = { _, _ ->
                openPicker()
            }
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "musicSelect.chooseMusic"
                this.isLocalizationKey = true
            })
        }
        bottomStage.elements += selectButton

        updateLabels()
        isChooserOpen = false
    }

    fun updateLabels(throwable: Throwable? = null) {
        if (throwable == null) {
            val music = editor.engine.music
            if (isLoading) {
                label.text = Localization["musicSelect.loadingMusic", "0"]
            } else {
                label.text = Localization["musicSelect.currentMusic",
                        if (music == null) Localization["musicSelect.noMusic"] else music.handle.name()]
                if (music != null) {
                    val start = music.music.getStartOfSound()
                    if (start >= 0f) {
                        label.text += "\n\n${Localization["musicSelect.estimatedMusicStart", (Editor.TRACKER_MINUTES_FORMATTER.format((start / 60).toLong()) + ":" + Editor.TRACKER_TIME_FORMATTER.format(start % 60.0))]}"
                    }
                }
            }
        } else {
            label.text = when (throwable) {
                is MusicLoadingException -> throwable.getLocalizedText()
                else -> Localization["musicSelect.invalid", throwable::class.java.canonicalName]
            }
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen) {
            GlobalScope.launch {
                isChooserOpen = true
                val filter = TinyFDWrapper.Filter(listOf("*.ogg", "*.mp3", "*.wav"), Localization["musicSelect.fileFilter"] + " (*.ogg, *.mp3, *.wav)")
                val file = TinyFDWrapper.openFile(Localization["musicSelect.fileChooserTitle"], attemptRememberDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_MUSIC)?.absolutePath?.plus("/"), false, filter)
                isChooserOpen = false
                if (file != null) {
                    loadFile(file)
                }
            }
        }
    }

    @Synchronized
    fun loadFile(file: File) {
        if (isLoading) return
        isLoading = true
        try {
            Gdx.app.postRunnable {
                updateLabels()
            }
            val handle = FileHandle(file)
            val engine = editor.engine
            val progressListener = { progress: Float ->
                if ((progress * 100).toInt() < 100) {
                    Gdx.app.postRunnable {
                        label.text = Localization["musicSelect.loadingMusic", (progress * 100).toInt()]
                    }
                }
            }
            val musicData = MusicData(handle, engine, progressListener)
            engine.music = musicData
            isLoading = false
            persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_MUSIC, file.parentFile)
            Gdx.app.postRunnable {
                updateLabels()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Gdx.app.postRunnable {
                updateLabels(t)
            }
        } finally {
            isLoading = false
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