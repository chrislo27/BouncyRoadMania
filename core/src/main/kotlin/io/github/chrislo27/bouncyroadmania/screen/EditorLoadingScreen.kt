package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.transition.TransitionScreen


class EditorLoadingScreen(main: BRManiaApp, val editorFactory: () -> EditorScreen)
    : ToolboksScreen<BRManiaApp, EditorLoadingScreen>(main) {

    override fun show() {
        super.show()

        Gdx.app.postRunnable {
            val screen = editorFactory()
            Gdx.app.postRunnable {
                main.screen = TransitionScreen(main, main.screen, screen, null, WipeFrom(Color.BLACK, 0.35f))
            }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}