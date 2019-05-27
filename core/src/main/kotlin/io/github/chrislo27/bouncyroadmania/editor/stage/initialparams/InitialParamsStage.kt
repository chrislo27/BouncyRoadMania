package io.github.chrislo27.bouncyroadmania.editor.stage.initialparams

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class InitialParamsStage(val editor: Editor, parent: EditorStage, palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera, parent.pixelsWidth, parent.pixelsHeight) {

    val engine: Engine get() = editor.engine

    val categoriesStage: Stage<EditorScreen>
    private val categories: List<CategoryStage>
    val coloursStage: ColoursStage
    val enginePropertiesStage: EnginePropertiesStage
    val resultsStage: ResultsStage
    val metadataStage: MetadataStage

    init {
        categoriesStage = Stage(this, this.camera, this.pixelsWidth, this.pixelsHeight).apply {
            this.location.set(screenY = 1f, screenHeight = 0f, pixelHeight = 32f, pixelY = -32f)
        }
        this.elements += categoriesStage

        coloursStage = ColoursStage(this, palette).apply {
            this.location.set(pixelHeight = -32f)
        }
        this.elements += coloursStage
        enginePropertiesStage = EnginePropertiesStage(this, palette).apply {
            this.location.set(pixelHeight = -32f)
        }
        this.elements += enginePropertiesStage
        resultsStage = ResultsStage(this, palette).apply {
            this.location.set(pixelHeight = -32f)
        }
        this.elements += resultsStage
        metadataStage = MetadataStage(this, palette).apply {
            this.location.set(pixelHeight = -32f)
        }
        this.elements += metadataStage

        categories = listOf(coloursStage, enginePropertiesStage, resultsStage, metadataStage)
        val categoryWidth = 1f / categories.size
        categories.forEachIndexed { i, category ->
            categoriesStage.elements += object : Button<EditorScreen>(palette, categoriesStage, categoriesStage) {
                val SELECTED_TINT: Color = Color(0.5f, 1f, 1f, 1f)
                override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    (labels.first() as TextLabel).textColor = if (category.visible) SELECTED_TINT else null
                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {
                this.location.set(screenX = i * categoryWidth, screenWidth = categoryWidth)
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.textWrapping = false
                    this.fontScaleMultiplier = 0.925f
                    this.isLocalizationKey = true
                    this.text = category.title
                })
                this.leftClickAction = { _, _ ->
                    categories.forEach { c -> c.visible = false }
                    category.visible = true
                }
            }
            if (i > 0) {
                categoriesStage.elements += ColourPane(categoriesStage, categoriesStage).apply {
                    this.colour.set(1f, 1f, 1f, 1f)
                    this.location.set(screenX = i * categoryWidth, screenWidth = 0f, pixelWidth = 1f)
                }
            }
            category.visible = i == 0
        }
        categoriesStage.elements += ColourPane(categoriesStage, categoriesStage).apply {
            this.colour.set(1f, 1f, 1f, 1f)
            this.location.set(screenHeight = 0f, pixelHeight = 1f)
        }
    }

    fun onEngineChange(engine: Engine) {
        categories.forEach { it.onEngineChange(engine) }
    }

}