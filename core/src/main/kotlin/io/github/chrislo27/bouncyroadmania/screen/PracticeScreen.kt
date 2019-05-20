package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.MusicData
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.transition.TransitionScreen
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class PracticeScreen(main: BRManiaApp, engine: Engine) : PlayingScreen(main, engine) {

    override fun onQuit() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    override fun onEnd() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }
    
}

/**
 * A black screen to hide loading of the practice music
 */
class LoadingPracticeScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, LoadingPracticeScreen>(main) {

    private fun createPracticeEngine(): Engine {
        val engine = Engine()
        engine.music = MusicData(Gdx.files.internal("music/practice.ogg"), engine)
        
        /*
        Order of events:
        Don't let the bouncing\nballs fall!
        Press [A] then [+] to bounce\nthe ball!
        [practice 2x with one-beat]
        Get ready!
        What, What? TWO balls\nincoming?!
        [practice 2x with two one-beat balls separated by two beats]
        Way to bounce!
        Uh-oh! The next ball is\ncoming in fast!
        [practice 2x with half-beat ball]
        Amazing!
        You've got the skills.\nReady for the real thing?
         */
        
        // Populate
        
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
                main.screen = TransitionScreen(main, main.screen, PracticeScreen(main, task.getCompleted()), null, WipeFrom(Color.BLACK, 0.35f))
            } else {
                main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), null, WipeFrom(Color.BLACK, 0.35f))
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
