package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class PlaybackButton(val playState: PlayState, val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    init {
        this.tooltipTextIsLocalizationKey = true
        this.tooltipText = playState.localizationKey
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        this.enabled = false
        when (editor.engine.playState) {
            PlayState.STOPPED -> if (playState == PlayState.PLAYING) enabled = true
            PlayState.PAUSED -> if (playState != PlayState.PAUSED) enabled = true
            PlayState.PLAYING -> if (playState != PlayState.PLAYING) enabled = true
        }
        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.engine.playState = this.playState
    }
}
