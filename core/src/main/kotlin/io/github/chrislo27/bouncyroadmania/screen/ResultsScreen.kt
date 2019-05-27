package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.input.Ranking
import io.github.chrislo27.bouncyroadmania.engine.input.Score
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import kotlin.properties.Delegates


class ResultsScreen(main: BRManiaApp, val score: Score)
    : ToolboksScreen<BRManiaApp, ResultsScreen>(main) {

//    enum class StagesE(val duration: Float) {
//        NONE(0.25f), TITLE(0.75f), LINE1(0.75f), LINE2(0.75f), SCORE_FILLING(145f / 60f), SCORE_REVEALED(0.75f), RANKING_REVEALED(1f);
//
//        companion object {
//            val LAST = RANKING_REVEALED
//            val VALUES = values().toList()
//        }
//    }

    sealed class Stages(val resultsScreen: ResultsScreen) {
        var timeout: Float = -1f
        var progress: Float = 0f

        open fun isDone(): Boolean {
            if (progress >= timeout)
                return true
            return false
        }

        open fun whenDone() {}
        open fun onStart() {}
        open fun update() {}

        abstract fun nextStage(): Stages

        class None(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = 1f
            }

            override fun nextStage(): Stages {
                return Ready(resultsScreen)
            }
        }

        class Ready(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = 1f
            }

            override fun nextStage(): Stages {
                return Title(resultsScreen)
            }
        }

        class Title(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = 1f
            }

            override fun onStart() {
                AssetRegistry.get<Sound>("sfx_results_first").play()
            }

            override fun nextStage(): Stages {
                return Line1(resultsScreen)
            }
        }

        class Line1(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = if (resultsScreen.score.line2.isNotEmpty()) 1f else 1.25f
            }

            override fun onStart() {
                resultsScreen.showFirstLine = true
                if (resultsScreen.score.line2.isNotEmpty()) {
                    AssetRegistry.get<Sound>("sfx_results_middle").play()
                } else {
                    AssetRegistry.get<Sound>("sfx_results_end").play()
                }
            }

            override fun nextStage(): Stages {
                return Line2(resultsScreen)
            }
        }

        class Line2(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = 1.25f
            }

            override fun isDone(): Boolean {
                return super.isDone() || resultsScreen.score.line2.isEmpty()
            }

            override fun onStart() {
                resultsScreen.showSecondLine = true
                if (resultsScreen.score.line2.isNotEmpty())
                    AssetRegistry.get<Sound>("sfx_results_end").play()
            }

            override fun nextStage(): Stages {
                return ScoreFilling(resultsScreen)
            }
        }

        class ScoreFilling(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = (145f / 60) * (resultsScreen.score.scoreInt / 100f)
            }

            override fun isDone(): Boolean {
                return super.isDone() || Gdx.input.isKeyJustPressed(Input.Keys.J)
            }

            override fun update() {
                resultsScreen.currentScoreScroll = MathUtils.lerp(0f, resultsScreen.score.scoreInt * 1f, (progress / timeout).coerceIn(0f, 1f)).toInt()
            }

            override fun whenDone() {
                resultsScreen.currentScoreScroll = resultsScreen.score.scoreInt
            }

            override fun nextStage(): Stages {
                return ScoreRevealed(resultsScreen)
            }
        }

        class ScoreRevealed(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            init {
                timeout = 0.75f
            }

            override fun onStart() {
                AssetRegistry.get<Sound>("sfx_score_finish").play()
            }

            override fun nextStage(): Stages {
                return RankingRevealed(resultsScreen)
            }
        }

        class RankingRevealed(resultsScreen: ResultsScreen) : Stages(resultsScreen) {
            override fun isDone(): Boolean {
                return false
            }

            override fun nextStage(): Stages {
                throw NotImplementedError()
            }

            override fun onStart() {
                AssetRegistry.get<Sound>(resultsScreen.score.ranking.sfx).play()
            }
        }
    }

    private val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
        update()
    }
    private var currentStage: Stages by Delegates.observable(Stages.None(this)) { _, oldValue: Stages, newValue: Stages ->
        if (oldValue != newValue) {
            oldValue.whenDone()
            newValue.onStart()
            if (oldValue is Stages.ScoreFilling) {
                if (fillingSoundId != -1L) {
                    fillingSound.stop(fillingSoundId)
                    fillingSoundId = -1L
                }
            } else if (newValue is Stages.ScoreFilling) {
                fillingSoundId = fillingSound.play()
            }
        }
    }

    private var fillingSoundId: Long = -1L
    private val fillingSound: Sound get() = AssetRegistry["sfx_score_filling"]
    private var showFirstLine = false
    private var showSecondLine = false
    private var aPressed = false
    private var currentScoreScroll: Int = -1

    override fun render(delta: Float) {
        val batch = main.batch

        batch.projectionMatrix = camera.combined
        batch.begin()

        val font = main.defaultFontLarge
        font.scaleFont(camera)
        font.scaleMul(0.5f)

        // Render text box
        val backing = AssetRegistry.get<Texture>("ui_textbox")
        val texW = backing.width
        val texH = backing.height
        val sectionX = texW / 3
        val sectionY = texH / 3
        val screenW = camera.viewportWidth
        val screenH = camera.viewportHeight
        val x = screenW * 0.15f
        val y = screenH * 0.8f
        val w = screenW * 0.7f
        val h = screenH / 6f
        if (currentStage !is Stages.None) {
            // Corners
            batch.draw(backing, x, y, sectionX * 1f, sectionY * 1f, 0f, 1f, 1 / 3f, 2 / 3f)
            batch.draw(backing, x, y + h - sectionY, sectionX * 1f, sectionY * 1f, 0f, 2 / 3f, 1 / 3f, 1f)
            batch.draw(backing, x + w - sectionX, y, sectionX * 1f, sectionY * 1f, 2 / 3f, 1f, 1f, 2 / 3f)
            batch.draw(backing, x + w - sectionX, y + h - sectionY, sectionX * 1f, sectionY * 1f, 2 / 3f, 2 / 3f, 1f, 1f)

            // Sides
            batch.draw(backing, x, y + sectionY, sectionX * 1f, h - sectionY * 2, 0f, 2 / 3f, 1 / 3f, 1 / 3f)
            batch.draw(backing, x + w - sectionX, y + sectionY, sectionX * 1f, h - sectionY * 2, 2 / 3f, 2 / 3f, 1f, 1 / 3f)
            batch.draw(backing, x + sectionX, y, w - sectionX * 2, sectionY * 1f, 1 / 3f, 0f, 2 / 3f, 1 / 3f)
            batch.draw(backing, x + sectionX, y + h - sectionY, w - sectionX * 2, sectionY * 1f, 1 / 3f, 2 / 3f, 2 / 3f, 1f)

            // Centre
            batch.draw(backing, x + sectionX, y + sectionY, w - sectionX * 2, h - sectionY * 2, 1 / 3f, 1 / 3f, 2 / 3f, 2 / 3f)
        }

        font.setColor(0f, 0f, 0f, 1f)
        // Render text
        if (currentStage !is Stages.None && currentStage !is Stages.Ready) {
            val textBoxText = score.title
            val textWidth = font.getTextWidth(textBoxText, w - sectionX * 2, false)
            val textHeight = font.getTextHeight(textBoxText)
            font.drawCompressed(batch, textBoxText, (x + w / 2f - textWidth / 2f).coerceAtLeast(x + sectionX), y + h / 2f + textHeight / 2,
                    w - sectionX * 2, Align.left)
        }
        
        font.setColor(1f, 1f, 1f, 1f)
        
        if (showFirstLine) {
            font.scaleMul(0.9f)
            val line1Height = font.getTextHeight(score.line1)
            font.drawCompressed(batch, score.line1, x + sectionX, y - 64f, w - sectionX * 2, Align.left)
            if (showSecondLine) {
                font.drawCompressed(batch, score.line2, x + sectionX, y - 64f - line1Height - font.capHeight, w - sectionX * 2, Align.left)
            }
            font.scaleMul(1f / 0.9f)
        }
        
        if (currentScoreScroll >= 0) {
            val scoreFont = main.kurokaneBorderedFont
            scoreFont.scaleFont(camera)
            scoreFont.scaleMul(0.75f)
            scoreFont.color = Ranking.getRanking(currentScoreScroll).color
            val width = scoreFont.draw(batch, currentScoreScroll.toString(), camera.viewportWidth / 2f, 260f, 0f, Align.center, false).width
            scoreFont.setColor(1f, 1f, 1f, 1f)
            scoreFont.scaleMul(1f / 0.75f)
            scoreFont.unscaleFont()
            
            if (currentScoreScroll == score.scoreInt) {
                var bonus = ""
                if (score.noMiss)
                    bonus += Localization["results.noMiss"]
                if (score.skillStar)
                    bonus += " [YELLOW]★[]"
                font.setColor(1f, 1f, 1f, 1f)
                font.draw(batch, bonus, camera.viewportWidth / 2f - width, 260f, 0f, Align.right, false)
            }
        }
        
        if (currentStage is Stages.RankingRevealed) {
            val rankFont: BitmapFont = when (score.ranking) {
                Ranking.TRY_AGAIN -> main.kurokaneBorderedFont
                Ranking.OK -> main.defaultBorderedFontLarge
                Ranking.SUPERB -> main.cometBorderedFont
            }
            val rankScale: Float = when (score.ranking) {
                Ranking.TRY_AGAIN -> 0.65f
                Ranking.OK -> 1f
                Ranking.SUPERB -> 0.9f
            }
            rankFont.scaleFont(camera)
            rankFont.scaleMul(rankScale)
            rankFont.color = score.ranking.color
            val rank = Localization[score.ranking.localization]
            val rankWidth = rankFont.getTextWidth(rank)
            rankFont.draw(batch, rank, x + w, 175f, 0f, Align.right, false)
            rankFont.setColor(1f, 1f, 1f, 1f)
            rankFont.scaleMul(1f / rankScale)
            rankFont.unscaleFont()
            if (score.butStillJustOk) {
                font.scaleMul(0.75f)
                font.draw(batch, Localization["results.butStillJust"], x + w - rankWidth * 1.25f, 175f, 0f, Align.right, false)
                font.scaleMul(1f / 0.75f)
            }
            
            if (currentStage.progress >= 1f) {
                val bordered = MathHelper.getSawtoothWave(1.25f) >= 0.75f || Gdx.input.isKeyPressed(Input.Keys.J)
                font.draw(batch, if (bordered) "\uE0A0" else "\uE0E0", camera.viewportWidth / 2f, 86f, 0f, Align.center, false)
                if (currentStage.progress >= 8f) {
                    font.scaleMul(0.5f)
                    font.draw(batch, "[LIGHT_GRAY][[J][]", camera.viewportWidth / 2f, 32f, 0f, Align.center, false)
                    font.scaleMul(1f / 0.5f)
                }
            }
        }

        font.scaleMul(1f / 0.5f)
        font.unscaleFont()

        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        val delta = Gdx.graphics.deltaTime
        currentStage.progress += delta
        currentStage.update()
        if (currentStage.isDone()) {
            currentStage = currentStage.nextStage()
        }

        if (Toolboks.debugMode) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                currentStage = Stages.None(this)
                aPressed = false
                showFirstLine = false
                showSecondLine = false
                currentScoreScroll = -1
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                main.screen = MainMenuScreen(main)
            }
        }
        
        if (currentStage is Stages.RankingRevealed) {
            if (!aPressed && Gdx.input.isKeyPressed(Input.Keys.J)) {
                aPressed = true
                AssetRegistry.get<Sound>("sfx_text_advance_1").play()
            } else if (aPressed && !Gdx.input.isKeyPressed(Input.Keys.J)) {
                aPressed = false
                AssetRegistry.get<Sound>("sfx_text_advance_2").play()
                main.screen = TransitionScreen(main, main.screen,
                        GameSelectScreen(main),
                        FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
            }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun getDebugString(): String {
        return "stage: ${currentStage::class.java.simpleName}\nprogress: ${currentStage.progress}"
    }
}