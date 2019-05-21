package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.GradientDirection
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.entity.Ball
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.Swing
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul


class TestEngineScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, TestEngineScreen>(main) {

    companion object {
        private val TMP_MATRIX = Matrix4()
    }

    val engine: Engine = Engine()

    var sendBallCycle = 0
    var hideUI = false

    init {
        reload()
    }

    fun reload() {
        engine.entities.clear()
        engine.playState = PlayState.PLAYING
        engine.seconds = 0f
        engine.tempos.clear()
        engine.tempos.add(TempoChange(engine.tempos, -5f, 154f, Swing.STRAIGHT, 0f))
        engine.gradientDirection = GradientDirection.VERTICAL
        sendBallCycle = 0

        engine.addBouncers()
        AssetRegistry.get<Music>("music_br").stop()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = main.batch

        engine.render(batch)

//        batch.begin()
//        val comet = main.cometBorderedFont
//        comet.scaleMul(0.5f)
////        comet.drawCompressed(batch, "Bouncy Road Mania", engine.camera.viewportWidth - 1280f / 2, comet.lineHeight * 1.75f, 600f, Align.right)
////        comet.drawCompressed(batch, "ホッピングロードマニア", engine.camera.viewportWidth - 1280f / 2, comet.lineHeight, 600f, Align.right)
//        comet.scaleMul(1f / 0.5f)
//        batch.end()
        if (!hideUI) {
            TMP_MATRIX.set(batch.projectionMatrix)
            batch.projectionMatrix = engine.camera.combined
            batch.begin()
            val font = main.defaultBorderedFont
            font.scaleFont(engine.camera)
            font.scaleMul(0.75f)
            font.drawCompressed(batch, "D - \uE110\n" +
                    "J - \uE0E0\nE - Deploy ball (0.5 ♩)\nSHIFT+E - Deploy (1 ♩)\nESC - Main Menu\nR - Stop\nM - Bouncy Road 1\nSHIFT+M - Bouncy Road 2\nF1 - Toggle UI",
                    engine.camera.viewportWidth - 600f - 8f, font.lineHeight * 9f, 600f, Align.right)
            font.scaleMul(1f / 0.75f)
            font.scaleMul(0.5f)
            font.drawCompressed(batch, BRMania.VERSION.toString(), 8f, 8f + font.capHeight, 400f, Align.left)
            font.scaleMul(1f / 0.5f)
            batch.end()
            batch.projectionMatrix = TMP_MATRIX
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()

        val delta = Gdx.graphics.deltaTime

        val ballCycle = 1f
        if (engine.beat > ballCycle * sendBallCycle) {
            sendBallCycle++
        }

        engine.update(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reload()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            engine.entities += Ball(engine, if (Gdx.input.isShiftDown()) 1f else 0.5f, engine.beat).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            hideUI = !hideUI
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            reload()
            engine.playState = PlayState.STOPPED
            AssetRegistry.get<Music>("music_br").play()
            Gdx.app.postRunnable {
                Gdx.app.postRunnable {
                    engine.playState = PlayState.PLAYING
                    engine.seconds = -1.94805f - 0.552f
                }
            }

            engine.entities += Ball(engine, 0.5f, 4f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 12f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 20f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 28f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.66666f, 38f, true).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.66666f, 54f, true).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            if (!Gdx.input.isShiftDown()) {
                engine.entities += Ball(engine, 1f, 68f - 1f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }
            }
            engine.entities += Ball(engine, 0.5f, 70f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 83f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 84f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 91f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 92f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 98f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 99f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 100f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }
            engine.entities += Ball(engine, 0.5f, 108f - 0.5f).apply {
                val first = engine.bouncers.first()
                posX = first.posX
                posY = first.posY
                bounce(engine.bouncers[0], engine.bouncers[1], false)
            }

            if (Gdx.input.isShiftDown()) {
                // Bouncy Road 2
                engine.entities += Ball(engine, 1f, -4f - 1f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }
                engine.entities += Ball(engine, 1f, 4f - 1f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }
                engine.entities += Ball(engine, 1f, 20f - 1f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }


                engine.entities += Ball(engine, 2f, 36f - 2f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }

                engine.entities += Ball(engine, 1f, 68.5f - 1f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }
                engine.entities += Ball(engine, 2f, 84f - 2f).apply {
                    val first = engine.bouncers.first()
                    posX = first.posX
                    posY = first.posY
                    bounce(engine.bouncers[0], engine.bouncers[1], false)
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            reload()
            main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
        }
    }


    override fun keyUp(keycode: Int): Boolean {
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

        return inputted || super.keyUp(keycode)
    }

    override fun keyDown(keycode: Int): Boolean {
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

        return inputted || super.keyDown(keycode)
    }

    override fun getDebugString(): String? {
        return """beat: ${engine.beat}
            |seconds: ${engine.seconds}
            |bpm: ${engine.tempos.tempoAt(engine.beat)}
        """.trimMargin()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}