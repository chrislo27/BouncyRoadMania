package io.github.chrislo27.bouncyroadmania.editor.stage.initialparams

import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class EnginePropertiesStage(ipStage: InitialParamsStage, palette: UIPalette)
    : CategoryStage(ipStage, palette, "initialParams.category.engineProperties") {

    val bouncerCountDown: Button<EditorScreen>
    val bouncerCountUp: Button<EditorScreen>
    val bouncerCount: TextLabel<EditorScreen>

    init {
        bouncerCountDown = Button(palette, this, this).apply {
            this.location.set(pixelX = 8f, screenX = 0f, screenWidth = 0f, pixelWidth = 40f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "▼"
                this.isLocalizationKey = false
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                engine.bouncerCount--
                updateLabels()
            }
        }
        elements += bouncerCountDown
        bouncerCount = TextLabel(palette, this, this).apply {
            this.location.set(pixelX = 8f + 40f, screenX = 0f, screenWidth = 0f, pixelWidth = 40f * 8, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textAlign = Align.center
            this.background = true
        }
        elements += bouncerCount
        bouncerCountUp = Button(palette, this, this).apply {
            this.location.set(pixelX = 8f + 40f * 9, screenX = 0f, screenWidth = 0f, pixelWidth = 40f, screenHeight = 0f, screenY = 1f, pixelY = -40f * 2, pixelHeight = 40f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "▲"
                this.isLocalizationKey = false
                this.textWrapping = false
            })
            this.leftClickAction = { _, _ ->
                engine.bouncerCount++
                updateLabels()
            }
        }
        elements += bouncerCountUp

        updateLabels()
    }

    fun updateLabels() {
        bouncerCountDown.enabled = engine.bouncerCount > Engine.MIN_BOUNCER_COUNT
        bouncerCountUp.enabled = engine.bouncerCount < Engine.MAX_BOUNCER_COUNT
        bouncerCount.text = Localization["initialParams.engineProperties.bouncerCount", engine.bouncerCount]
    }

    override fun onEngineChange(engine: Engine) {
        updateLabels()
    }
}