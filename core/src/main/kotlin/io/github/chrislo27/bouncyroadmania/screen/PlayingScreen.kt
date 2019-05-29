package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.discord.DiscordHelper
import io.github.chrislo27.bouncyroadmania.discord.PresenceState
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown


open class PlayingScreen(main: BRManiaApp, val engine: Engine) : ToolboksScreen<BRManiaApp, PlayingScreen>(main) {

    data class PauseState(val lastPlayState: PlayState)

    final override val stage: Stage<PlayingScreen> = Stage(null, main.defaultCamera, 1280f, 720f)
    protected val robotModeButton: Button<PlayingScreen>
    protected val resumeButton: Button<PlayingScreen>
    protected val restartButton: Button<PlayingScreen>
    protected val quitButton: Button<PlayingScreen>

    protected var robotEnabled: Boolean = false
    protected var paused: PauseState? = null

    init {
        val palette = main.uiPalette
        stage.visible = false
        stage.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }
        stage.elements += ColourPane(stage, stage).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
        }
        stage.elements += TextLabel(palette.copy(ftfont = main.kurokaneBorderedFontFTF), stage, stage).apply {
            this.textAlign = Align.center
            this.textWrapping = false
            this.text = "playing.pauseMenu.title"
            this.isLocalizationKey = true
            this.fontScaleMultiplier = 0.75f
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 500f, pixelHeight = 96f)
        }
        resumeButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 400f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.resume"
            })
            this.leftClickAction = { _, _ ->
                pauseUnpause()
            }
        }
        stage.elements += resumeButton
        restartButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 300f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(object : TextLabel<PlayingScreen>(palette, this, this.stage){
                override fun getRealText(): String {
                    return if (Gdx.input.isShiftDown()) Localization["playing.pauseMenu.restart.withRobotMode"] else super.getRealText()
                }
            }.apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.restart"
            })
            this.leftClickAction = { _, _ ->
                engine.playState = PlayState.STOPPED
                playStartOverSfx()
                val shiftDown = Gdx.input.isShiftDown()
                reset(0.5f, robotMode = shiftDown)
                if (shiftDown) {
                    AssetRegistry.get<Sound>("sfx_robot_on").play()
                }
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "playing.pauseMenu.restart.tooltip"
        }
        stage.elements += restartButton
        quitButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 200f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.pauseMenu.quit"
            })
            this.leftClickAction = { _, _ ->
                engine.playState = PlayState.STOPPED
                engine.playState = PlayState.PAUSED
                playSelectSfx()
                onQuit()
            }
        }
        stage.elements += quitButton
        robotModeButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 50f, pixelHeight = 64f, screenX = 0.35f, screenWidth = 0.3f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.robot.off"
            })
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "playing.robot.tooltip"
            this.leftClickAction = { _, _ ->
                robotEnabled = true
                engine.requiresPlayerInput = !engine.requiresPlayerInput
                AssetRegistry.get<Sound>("sfx_robot_${if (!engine.requiresPlayerInput) "on" else "off"}").play()
                (labels.first() as TextLabel).text = "playing.robot.${if (!engine.requiresPlayerInput) "on" else "off"}"
            }
        }
        stage.elements += robotModeButton

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

    init {
        reset()
    }

    override fun render(delta: Float) {
        val batch = main.batch

        engine.render(batch)

        stage.visible = paused != null
        if (paused == null && main.screen == this) {
            if (!Gdx.input.isCursorCatched) {
                Gdx.input.isCursorCatched = true
            }
        } else {
            if (Gdx.input.isCursorCatched) {
                Gdx.input.isCursorCatched = false
            }
        }
        super.render(delta)
    }

    private fun reset(secondsBefore: Float = 1f, robotMode: Boolean = false) {
        robotEnabled = robotMode
        engine.requiresPlayerInput = !robotMode
        paused = null
        (robotModeButton.labels.first() as TextLabel).text = "playing.robot.${if (robotMode) "on" else "off"}"
        engine.resetInputs()
        engine.playbackStart = engine.tempos.secondsToBeats(engine.tempos.beatsToSeconds(engine.playbackStart) - secondsBefore)
        engine.playState = PlayState.PLAYING
    }
    
    protected open fun playSelectSfx() {
        AssetRegistry.get<Sound>("sfx_select").play()
    }

    protected open fun playStartOverSfx() {
        AssetRegistry.get<Sound>("sfx_enter_game").play()
    }

    protected open fun onQuit() {
        main.screen = TransitionScreen(main, main.screen, GameSelectScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    protected open fun onEnd() {
        // Transition away to proper results
        val score = engine.computeScore()
        if (robotEnabled) {
            main.screen = TransitionScreen(main, main.screen,
                    GameSelectScreen(main),
                    FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
        } else {
            main.screen = TransitionScreen(main, main.screen,
                    ResultsScreen(main, score),
                    FadeOut(1f, Color.BLACK), null)
        }
    }

    fun pauseUnpause() {
        val paused = this.paused
        if (paused == null) {
            this.paused = PauseState(engine.playState)
            engine.playState = PlayState.PAUSED
            AssetRegistry.get<Sound>("sfx_pause_enter").play()
        } else {
            this.paused = null
            engine.playState = paused.lastPlayState
            AssetRegistry.get<Sound>("sfx_pause_exit").play()
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        val delta = Gdx.graphics.deltaTime

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseUnpause()
        }

        if (paused == null) {
            engine.update(delta)

            if (engine.playState == PlayState.STOPPED) {
                onEnd()
            }
        }
    }

    override fun keyUp(keycode: Int): Boolean {
        if (paused == null) {
            var inputted = false
            if (keycode == Input.Keys.D) {
                if (engine.requiresPlayerInput) {
                    engine.fireInput(InputType.DPAD, false)
                    inputted = true
                }
            }
            if (keycode == Input.Keys.J) {
                if (engine.requiresPlayerInput) {
                    engine.fireInput(InputType.A, false)
                    inputted = true
                }
            }
            return inputted
        }
        return super.keyUp(keycode)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (paused == null) {
            var inputted = false
            if (keycode == Input.Keys.D) {
                if (engine.requiresPlayerInput) {
                    engine.fireInput(InputType.DPAD, true)
                    inputted = true
                }
            }
            if (keycode == Input.Keys.J) {
                if (engine.requiresPlayerInput) {
                    engine.fireInput(InputType.A, true)
                    inputted = true
                }
            }
            return inputted
        }
        return super.keyDown(keycode)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun show() {
        super.show()
        DiscordHelper.updatePresence(PresenceState.Playing)
    }

    override fun hide() {
        super.hide()
        engine.dispose()
        Gdx.input.isCursorCatched = false
    }

    override fun getDebugString(): String? {
        return engine.getDebugString()
    }
}