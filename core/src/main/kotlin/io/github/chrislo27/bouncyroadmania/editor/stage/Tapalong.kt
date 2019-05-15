package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.bouncyroadmania.editor.EditMode
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


class TapalongButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {

    override var tooltipText: String?
        get() = if (editor.editMode == EditMode.EVENTS) "editor.tapalong" else "editor.tapalong.disabled"
        set(_) {}

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_tapalong_button"))
        })
        this.tooltipTextIsLocalizationKey = true
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editorStage.setParamsStage(TapalongStage(editorStage))
    }
}

class TapalongStage(parent: EditorStage) : ParamsStage(parent) {

    override val mustCloseWhenPlaying: Boolean = false

    init {
        title.isLocalizationKey = true
        title.text = "editor.tapalong"
    }
}
