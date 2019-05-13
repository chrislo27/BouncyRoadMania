package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.bouncyroadmania.stage.GenericStage
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


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
        // TODO
        Gdx.app.postRunnable {
            editorStage.elements += MenuOverlay(editor, editorStage, palette).apply {
                elements += MusicMenuStage(palette, this)
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

class MusicMenuStage(palette: UIPalette, parent: Stage<EditorScreen>)
    : GenericStage<EditorScreen>(palette, parent, parent.camera) {

    init {
        onBackButtonClick = {
            Gdx.app.postRunnable {
                (parent.parent as Stage).elements.remove(parent)
            }
        }
        backButton.visible = true
        titleLabel.text = "Test Title"
        titleLabel.isLocalizationKey = false
        titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_song_choose"))
        centreStage.elements += TextLabel(palette, centreStage, centreStage).apply {
            this.isLocalizationKey = false
            this.text = "Some test text content here"
        }
    }

}