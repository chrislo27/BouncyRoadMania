package io.github.chrislo27.bouncyroadmania.editor.stage

import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


class TimelineStage(parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
        }
        val editor = parent.editor

        // Bottom separator
        this.elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 0.5f)
            this.location.set(0f, 0f, 1f, 0f, pixelHeight = 1f)
        }
    }

}