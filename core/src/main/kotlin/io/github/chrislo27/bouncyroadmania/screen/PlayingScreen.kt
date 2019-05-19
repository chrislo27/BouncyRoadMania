package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import kotlin.math.roundToInt


class PlayingScreen(main: BRManiaApp, val engine: Engine) : ToolboksScreen<BRManiaApp, PlayingScreen>(main) {

    override val stage: Stage<PlayingScreen> = Stage(null, main.defaultCamera, 1280f, 720f)

    init {
        engine.playbackStart = engine.tempos.secondsToBeats(engine.tempos.beatsToSeconds(engine.playbackStart) - 1f)
        engine.playState = PlayState.PLAYING
    }

    init {
        val palette = main.uiPalette
        stage.visible = false
        stage.elements += ColourPane(stage, stage).apply {
            this.colour.set(0f, 0f, 0f, 0.75f)
        }
        stage.elements += TextLabel(palette.copy(ftfont = main.kurokaneBorderedFontFTF), stage, stage).apply {
            this.textAlign = Align.center
            this.textWrapping = false
            this.text = "playing.pauseMenu.title"
            this.isLocalizationKey = true
            this.fontScaleMultiplier = 0.75f
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 500f, pixelHeight = 96f)
        }
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 400f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.resume"
            })
            this.leftClickAction = { _, _ ->
                engine.playState = PlayState.PLAYING
            }
        }
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 300f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.restart"
            })
            this.leftClickAction = { _, _ ->
                engine.playState = PlayState.STOPPED
                engine.playState = PlayState.PLAYING
            }
        }
        stage.elements += Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 200f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.quit"
            })
            this.leftClickAction = { _, _ ->
                engine.playState = PlayState.STOPPED
                engine.playState = PlayState.PAUSED
                main.screen = TransitionScreen(main, main.screen, GameSelectScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
            }
        }
        
        stage.elements += TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), stage, stage).apply {
            this.location.set(screenX = 0f, screenWidth = 0f, screenHeight = 0f, pixelX = 0f, pixelWidth = 220f, pixelHeight = 96f)
            this.isLocalizationKey = false
            this.text = " Game Controls:\n [YELLOW]\uE0E0[] - J\n [RED]\uE110[] - D"
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = 1f
            this.background = true
        }

        stage.updatePositions()
    }

    override fun render(delta: Float) {
        val batch = main.batch

        engine.render(batch)

        stage.visible = engine.playState == PlayState.PAUSED
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        val delta = Gdx.graphics.deltaTime

        engine.update(delta)

        if (engine.playState == PlayState.STOPPED) {
            // Transition away to proper results TODO
            main.screen = TransitionScreen(main, main.screen,
                    GameSelectScreen(main, Localization["playing.tmpResults", engine.computeScore().roundToInt(), Localization["gameSelect.select"]]),
                    FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
//            main.screen = TransitionScreen(main, main.screen, GameSelectScreen(main), FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            when (engine.playState) {
                PlayState.STOPPED -> {
                    // ignored
                }
                PlayState.PLAYING -> {
                    engine.playState = PlayState.PAUSED
                }
                PlayState.PAUSED -> {
                    engine.playState = PlayState.PLAYING
                }
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (engine.playState == PlayState.PLAYING) {
                engine.fireInput(InputType.DPAD)
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (engine.playState == PlayState.PLAYING) {
                engine.fireInput(InputType.A)
            }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun hide() {
        super.hide()
        engine.dispose()
    }
}