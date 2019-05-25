package io.github.chrislo27.bouncyroadmania.editor.stage.initialparams

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


abstract class CategoryStage(val ipStage: InitialParamsStage, val palette: UIPalette, val title: String)
    : Stage<EditorScreen>(ipStage, ipStage.camera, ipStage.pixelsWidth, ipStage.pixelsHeight) {

    protected val engine: Engine get() = ipStage.engine
    
    open fun onEngineChange(engine: Engine) {

    }
}