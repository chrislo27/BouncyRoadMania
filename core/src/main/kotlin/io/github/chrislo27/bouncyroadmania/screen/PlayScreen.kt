package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.util.MusicCredit
import io.github.chrislo27.bouncyroadmania.util.fadeTo
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen


class PlayScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, MainMenuScreen>(main) {

    companion object {
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("<SOMETHING>") }
    }

    val music: Music by lazy {
        AssetRegistry.get<Music>("music_play_screen").apply {
            isLooping = true
        }
    }
    
    init {
        music.volume = if (main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)) 0f else 1f
        music.play()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            backToMainMenu()
        }
    }
    
    private fun backToMainMenu() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    override fun show() {
        super.show()
        music.play()
    }

    override fun hide() {
        super.hide()
        music.fadeTo(0f, 0.25f)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}