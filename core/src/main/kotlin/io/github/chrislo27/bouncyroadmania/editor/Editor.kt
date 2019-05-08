package io.github.chrislo27.bouncyroadmania.editor

import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import kotlin.properties.Delegates


class Editor {

    val stage: EditorStage = EditorStage(this)
    var editMode: EditMode by Delegates.observable(EditMode.EVENTS) { _, oldVal, newVal ->

    }
    val engine: Engine = Engine()
    var currentTool: Tool = Tool.SELECTION

}