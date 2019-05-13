package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.Tool
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class ToolButton(val editor: Editor, val tool: Tool,
                 palette: UIPalette, parent: PickerStage)
    : Button<EditorScreen>(palette, parent, parent) {

    val selectorRegionSeries: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector")) }

    val label: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
        this.renderType = ImageLabel.ImageRendering.RENDER_FULL
    }
    var selected: Boolean = false
        set(value) {
            val old = field
            field = value
            if (field != old) {
                this.removeLabel(selectedLabel)
                if (field) {
                    this.addLabel(1, selectedLabel)
                    selectedLabel.onResize(this.location.realWidth, this.location.realHeight, 1f, 1f)
                }
            }
        }
    val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
        this.image = selectorRegionSeries
    }

    override var tooltipText: String?
        get() = Localization[tool.nameId] + keyText
        set(_) {}

    init {
        addLabel(label)
    }

    private val keyText: String by lazy {
        when (val moddedIndex = tool.index + 1) {
            10 -> " [LIGHT_GRAY][[0][]"
            in 1..9 -> " [LIGHT_GRAY][[$moddedIndex][]"
            else -> ""
        } + (if (tool.keybinds.isNotEmpty()) " [LIGHT_GRAY]${tool.keybinds.joinToString(" ") { "[[$it]" }}[]" else "")
    }

    init {
        label.image = TextureRegion(tool.texture)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (editor.clickOccupation == ClickOccupation.None) {
            editor.currentTool = tool
            (parent as PickerStage).updateLabels()
        }
        editor.updateMessageBar()
    }
}