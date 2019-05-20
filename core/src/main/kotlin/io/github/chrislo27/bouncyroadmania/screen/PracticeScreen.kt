package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.MusicData
import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.practice.SpawnTextBoxEvent
import io.github.chrislo27.bouncyroadmania.engine.event.practice.XMoreTimesEvent
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.Swing
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.transition.TransitionScreen
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class PracticeScreen(main: BRManiaApp, engine: Engine) : PlayingScreen(main, engine) {

    init {
        restartButton.enabled = false
        robotModeButton.visible = false
    }
    
    override fun onQuit() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    override fun onEnd() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
    }
    
}

/**
 * A black screen to hide loading of the practice music
 */
class LoadingPracticeScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, LoadingPracticeScreen>(main) {

    private fun createPracticeEngine(): Engine {
        val engine = Engine()
        engine.music = MusicData(Gdx.files.internal("music/practice.ogg"), engine).apply {
            this.music.setLooping(true)
        }
        
        /*
        Order of events:
        Don't let the bouncing\nballs fall!
        Press [A] then [+] to bounce\nthe ball!
        [practice 2x with one-beat]
        Get ready!
        What, what? TWO balls\nincoming?!
        [practice 2x with two one-beat balls separated by two beats]
        Way to bounce!
        Uh-oh! The next ball is\ncoming in fast!
        [practice 2x with half-beat ball]
        Amazing!
        You've got the skills.\nReady for the real thing?
         */
        
        // Populate
        with(engine) {
            playbackStart = 0f
            musicStartSec = 1.5f
            tempos.add(TempoChange(tempos, 0f, 60f, Swing.STRAIGHT, 0f))
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.stage1-1"], true)).apply {
                this.bounds.x = 0f
                this.bounds.width = 0.5f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.stage1-2"], true)).apply {
                this.bounds.x = 0.5f
                this.bounds.width = 0.5f
            })
            addEvent(XMoreTimesEvent(this, 2).apply {
                this.bounds.x = 1.5f
                this.bounds.width = 0.5f
            })
            tempos.add(TempoChange(tempos, 1.5f, 154f, Swing.STRAIGHT, 0f))
        }
        
        return engine
    }
    
    private val task: Deferred<Engine> = GlobalScope.async { createPracticeEngine() }
    
    override fun render(delta: Float) {
        super.render(delta)
    }

    @ExperimentalCoroutinesApi
    override fun renderUpdate() {
        super.renderUpdate()
        if (task.isCompleted) {
            val exception = task.getCompletionExceptionOrNull()
            if (exception != null) {
                exception.printStackTrace()
                main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), null, WipeFrom(Color.BLACK, 0.35f))
            } else {
                main.screen = TransitionScreen(main, main.screen, PracticeScreen(main, task.getCompleted()), null, WipeFrom(Color.BLACK, 0.35f))
            }
        } else if (task.isCancelled) {
            main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), null, WipeFrom(Color.BLACK, 0.35f))
        }
    }


    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
