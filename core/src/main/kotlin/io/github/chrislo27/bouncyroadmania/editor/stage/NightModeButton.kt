package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.EditorTheme
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


class NightModeButton(val editor: Editor, palette: UIPalette, parent: Stage<EditorScreen>, val editorStage: EditorStage)
    : Button<EditorScreen>(palette, parent, parent) {
    
    val night = TextureRegion(AssetRegistry.get<Texture>("ui_nighttime"))
    val day = TextureRegion(AssetRegistry.get<Texture>("ui_daytime"))
    val label = ImageLabel(palette, this, this.stage)
    
    init {
        this.tooltipTextIsLocalizationKey = true
        addLabel(label)
        updateLabel()
    }
    
    fun updateLabel() {
        val themes = EditorTheme.DEFAULT_THEMES
        if (editor.theme == themes["dark"]) {
            this.tooltipText = "editor.theme.day"
            label.image = day
        } else {
            this.tooltipText = "editor.theme.night"
            label.image = night
        }
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)

        val themes = EditorTheme.DEFAULT_THEMES
        if (editor.theme == themes["light"]) {
            editor.main.editorTheme = themes.getValue("dark")
        } else {
            editor.main.editorTheme = themes.getValue("light")
        }
        updateLabel()
    }
}