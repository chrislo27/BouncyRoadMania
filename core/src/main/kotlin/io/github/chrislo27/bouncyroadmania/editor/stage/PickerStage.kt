package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.Tool
import io.github.chrislo27.bouncyroadmania.registry.EventRegistry
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class PickerStage(val editor: Editor, parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    val display: PickerDisplay
    val displayArrow: TextLabel<EditorScreen>
    val displayScrollUp: Button<EditorScreen>
    val displayScrollDown: Button<EditorScreen>
    val summary: TextLabel<EditorScreen>
    val desc: TextLabel<EditorScreen>
    val toolButtons: List<ToolButton>

    init {
        val main = editor.main
        val arrowPalette = palette.copy(ftfont = main.defaultBorderedFontFTF)

        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
            this.location.set(0f, 0f, 1f, 1f)
        }

        val separator = 0.35f
        // Scrolling display
        display = PickerDisplay(this, parent.editor).apply {
            this.location.set(0f, 0f, separator, 1f, 48f, 0f, -48f, 0f)
        }
        elements += display

        displayArrow = TextLabel(arrowPalette, this, this).apply {
            this.isLocalizationKey = false
            this.text = Editor.ARROWS[4]
            this.location.set(screenY = 0.5f, screenWidth = 0f, screenHeight = 0f, pixelWidth = 48f, pixelHeight = 48f, pixelY = -24f)
        }
        elements += displayArrow
        displayScrollUp = Button(arrowPalette, this, this).apply {
            addLabel(TextLabel(arrowPalette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = Editor.ARROWS[2]
            })
            this.location.set(screenY = 1f, screenWidth = 0f, screenHeight = 0f, pixelWidth = 48f, pixelHeight = 48f, pixelY = -48f)
            this.background = false
            this.leftClickAction = { _, _ ->
                if (display.index > 0) display.index--
            }
        }
        elements += displayScrollUp
        displayScrollDown = Button(arrowPalette, this, this).apply {
            addLabel(TextLabel(arrowPalette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = Editor.ARROWS[3]
            })
            this.location.set(screenY = 0f, screenWidth = 0f, screenHeight = 0f, pixelWidth = 48f, pixelHeight = 48f, pixelY = 0f)
            this.background = false
            this.leftClickAction = { _, _ ->
                if (display.index < EventRegistry.list.size - 1) display.index++
            }
        }
        elements += displayScrollDown

        // Separator
        elements += ColourPane(this, this).apply {
            this.colour.set(1f, 1f, 1f, 1f)
            this.location.set(separator, 0f, 0f, 1f, 0f, 0f, 1f, 0f)
        }

        // Title and description
        val titleAmt = 0.2f
        summary = TextLabel(palette, this, this).apply {
            this.location.set(screenHeight = titleAmt * 0.75f, screenY = 1f - titleAmt, screenX = separator, screenWidth = 1f - separator, pixelX = 8f, pixelWidth = -16f - (Tool.VALUES.size * 32f))
            this.isLocalizationKey = false
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = 1f
            this.text = ""
        }
        elements += summary
        desc = TextLabel(palette, this, this).apply {
            this.location.set(screenHeight = 1f - titleAmt, screenX = separator, screenWidth = 1f - separator, pixelX = 8f, pixelWidth = -16f)
            this.isLocalizationKey = false
            this.textAlign = Align.topLeft
            this.textWrapping = false
            this.fontScaleMultiplier = 0.75f
            this.text = ""
        }
        elements += desc

        toolButtons = mutableListOf()
        val numTools = Tool.VALUES.size
        Tool.VALUES.forEachIndexed { i, tool ->
            toolButtons += ToolButton(editor, tool, palette, this).apply {
                this.location.set(screenX = 1f, screenY = 1f, screenWidth = 0f, screenHeight = 0f, pixelX = (numTools - i) * -32f, pixelY = -32f, pixelWidth = 32f, pixelHeight = 32f)
            }
        }
        elements.addAll(toolButtons)

        updateLabels()
    }

    fun updateLabels() {
        val index = display.index
        val list = EventRegistry.list
        if (list.isEmpty()) {
            summary.text = ""
            desc.text = ""
        } else {
            val instantiator = list[index]
            summary.text = instantiator.displaySummary
            desc.text = instantiator.displayDesc
        }
        toolButtons.forEach {
            it.selected = it.tool == editor.currentTool
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        displayArrow.textColor = if (editor.currentTool == Tool.SELECTION) PickerDisplay.SELECTED_TINT else null
        (displayScrollUp.labels.first() as TextLabel).text = if (display.index <= 0) Editor.ARROWS[2] else Editor.ARROWS[0]
        (displayScrollDown.labels.first() as TextLabel).text = if (display.index >= EventRegistry.list.size - 1) Editor.ARROWS[3] else Editor.ARROWS[1]
        super.render(screen, batch, shapeRenderer)
    }
}