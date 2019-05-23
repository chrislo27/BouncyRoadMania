package io.github.chrislo27.bouncyroadmania.editor.oopsies.impl

import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction


class TrackResizeAction(val oldSize: Int, val newSize: Int)
    : ReversibleAction<Editor> {

    override fun redo(context: Editor) {
        context.engine.trackCount = newSize
    }

    override fun undo(context: Editor) {
        context.engine.trackCount = oldSize
    }

}