package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.discord.DiscordHelper
import io.github.chrislo27.bouncyroadmania.discord.PresenceState
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


open class PlayingScreen(main: BRManiaApp, val engine: Engine) : ToolboksScreen<BRManiaApp, PlayingScreen>(main) {

    data class PauseState(val lastPlayState: PlayState)
    
    companion object {
        protected val PAUSED_TITLE_MATRIX = Matrix4().rotate(0f, 0f, 1f, 20.556f).translate(220f, 475f, 0f)
        protected val TMP_CAMERA_MATRIX = Matrix4()
    }

    final override val stage: Stage<PlayingScreen> = Stage(null, main.defaultCamera, 1280f, 720f)
    protected val robotModeButton: Button<PlayingScreen>
    protected val resumeButton: Button<PlayingScreen>
    protected val restartButton: Button<PlayingScreen>
    protected val quitButton: Button<PlayingScreen>

    protected var robotEnabled: Boolean = false
    protected var paused: PauseState? = null
    
    private var isCursorInvisible = false
    
    protected val pauseCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }

    init {
        val palette = main.uiPalette
        stage.visible = false
        stage.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }
        resumeButton = Button(palette, stage, stage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 275f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
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
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 200f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
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
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 125f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
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
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 125f, pixelHeight = 64f, screenX = 0.3f, screenWidth = 0.2f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "playing.robot.${if (!engine.requiresPlayerInput) "on" else "off"}"
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
        reset(robotMode = !engine.requiresPlayerInput)
    }

    override fun render(delta: Float) {
        val batch = main.batch

        engine.render(batch)

        stage.visible = paused != null
        
        if (paused != null) {
            val shapeRenderer = main.shapeRenderer
            pauseCamera.update()
            batch.projectionMatrix = pauseCamera.combined
            shapeRenderer.projectionMatrix = pauseCamera.combined
            batch.begin()
            
            batch.setColor(1f, 1f, 1f, 0.5f)
            batch.fillRect(0f, 0f, 1280f, 720f)
            batch.setColor(1f, 1f, 1f, 1f)
            
            shapeRenderer.prepareStencilMask(batch) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.triangle(0f, 720f, 1280f * 0.75f, 720f, 0f, 720f * 0.5f)
                shapeRenderer.triangle(1280f * 0.25f, 0f, 1280f, 0f, 1280f, 720f * 0.5f)
                shapeRenderer.end()
            }.useStencilMask {
                batch.setColor(1f, 1f, 1f, 1f)
                val tex: Texture = AssetRegistry["tex_pause_bg"]
                val period = 5f
                val start: Float = MathHelper.getSawtoothWave(period)
                val speed = 1f

                val w = tex.width
                val h = tex.height
                for (x in (start * w * speed - (w * speed.absoluteValue)).toInt()..pauseCamera.viewportWidth.roundToInt() step w) {
                    for (y in (start * h * speed - (h * speed.absoluteValue)).toInt()..pauseCamera.viewportHeight.roundToInt() step h) {
                        batch.draw(tex, x.toFloat(), y.toFloat(), w * 1f, h * 1f)
                    }
                }
            }
            
            val titleFont = main.kurokaneBorderedFont
            titleFont.scaleFont(pauseCamera)
            titleFont.scaleMul(0.9f)
            batch.projectionMatrix = TMP_CAMERA_MATRIX.set(pauseCamera.combined).mul(PAUSED_TITLE_MATRIX)
            titleFont.drawCompressed(batch, Localization["playing.pauseMenu.title"], 0f, 0f, 475f, Align.center)
            batch.projectionMatrix = pauseCamera.combined
            titleFont.unscaleFont()

            if (engine.skillStarInput.isFinite()) {
                val texColoured = AssetRegistry.get<Texture>("tex_skill_star")
                val texGrey = AssetRegistry.get<Texture>("tex_skill_star_grey")

//                val scale = Interpolation.exp10.apply(1f, 2f, (skillStarPulseAnimation).coerceAtMost(1f))
//                val rotation = Interpolation.exp10Out.apply(0f, 360f, 1f - skillStarSpinAnimation)
                batch.draw(if (engine.gotSkillStar) texColoured else texGrey, 1184f, 32f, 64f, 64f)
            }
            
            batch.end()
            shapeRenderer.projectionMatrix = main.defaultCamera.combined
            batch.projectionMatrix = main.defaultCamera.combined
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
        
        if (paused == null && main.screen == this) {
            if (!isCursorInvisible) {
                isCursorInvisible = true
                Gdx.graphics.setCursor(AssetRegistry["cursor_invisible"])
            }
        } else {
            if (isCursorInvisible) {
                isCursorInvisible = false
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
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
        isCursorInvisible = false
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }

    override fun getDebugString(): String? {
        return engine.getDebugString()
    }
}