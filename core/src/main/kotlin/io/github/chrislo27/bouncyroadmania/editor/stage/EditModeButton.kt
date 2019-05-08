package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class EditModeButton(val editMode: EditMode, val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    init {
        this.tooltipTextIsLocalizationKey = true
        this.tooltipText = editMode.localizationKey
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (labels.isNotEmpty()) {
            val imageLabel = labels.first() as ImageLabel
            if (editor.editMode == this.editMode) {
                imageLabel.tint.set(0f, 1f, 1f, 1f)
            } else {
                imageLabel.tint.set(1f, 1f, 1f, 1f)
            }
        }
        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.editMode = this.editMode
    }
}