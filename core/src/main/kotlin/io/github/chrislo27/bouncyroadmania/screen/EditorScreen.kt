package io.github.chrislo27.bouncyroadmania.screen

import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.toolboks.ToolboksScreen


class EditorScreen(main: BRManiaApp) : ToolboksScreen<BRManiaApp, EditorScreen>(main) {

    val editor: Editor = Editor()
    override val stage: EditorStage
        get() = editor.stage

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}