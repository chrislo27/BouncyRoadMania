package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.transition.TransitionScreen


class EditorLoadingScreen(main: BRManiaApp, val editorFactory: () -> EditorScreen)
    : ToolboksScreen<BRManiaApp, EditorLoadingScreen>(main) {

    override fun show() {
        super.show()

        main.screen = TransitionScreen(main, main.screen, editorFactory(), null, WipeFrom(Color.BLACK, 0.35f))
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}