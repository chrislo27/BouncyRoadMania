package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.util.MusicCredit
import io.github.chrislo27.bouncyroadmania.util.fadeTo
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class PlayScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, MainMenuScreen>(main) {

    companion object {
        private val MUSIC_CREDIT: String by lazy { MusicCredit.credit("Faster Does It") }
    }

    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    override val stage: Stage<MainMenuScreen> = Stage(null, main.defaultCamera, camera.viewportWidth, camera.viewportHeight)
    val music: Music by lazy {
        AssetRegistry.get<Music>("music_play_screen").apply {
            isLooping = true
        }
    }
    
    init {
        stage.tooltipElement = TextLabel(main.uiPalette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }
        stage.elements += object : Button<MainMenuScreen>(main.uiPalette, stage, stage) {
            val unmuted = TextureRegion(AssetRegistry.get<Texture>("ui_music"))
            val muted = TextureRegion(AssetRegistry.get<Texture>("ui_music_muted"))
            val label = ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            }

            init {
                addLabel(label)
            }
        }.apply {
            this.location.set(screenWidth = 0f, screenHeight = 0f,
                    pixelWidth = 32f, pixelHeight = 32f, pixelX = camera.viewportWidth - 32f, pixelY = camera.viewportHeight - 32f)
            val muted = main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)
            music.volume = if (muted) 0f else 1f
            label.image = if (muted) this.muted else unmuted
            this.leftClickAction = { _, _ ->
                val old = main.preferences.getBoolean(PreferenceKeys.MUTE_MUSIC, false)
                main.preferences.putBoolean(PreferenceKeys.MUTE_MUSIC, !old).flush()
                music.volume = if (!old) 0f else 1f
                label.image = if (!old) this@apply.muted else unmuted
                this.tooltipText = "mainMenu.tooltip.${if (old) "mute" else "unmute"}Music"
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "mainMenu.tooltip.${if (!muted) "mute" else "unmute"}Music"
        }
        stage.elements += Button(main.uiPalette, stage, stage).apply {
            this.location.set(screenX = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = -450f - 4f, pixelWidth = 450f, pixelHeight = 16f * 3)
            this.addLabel(TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = MUSIC_CREDIT
                this.textAlign = Align.right
                this.textWrapping = false
                this.fontScaleMultiplier = 0.5f
            })
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("https://incompetech.com")
            }
        }

        stage.updatePositions()
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