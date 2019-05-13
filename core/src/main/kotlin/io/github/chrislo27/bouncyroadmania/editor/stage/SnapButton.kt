package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*

class SnapButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    companion object {
        val snapLevels = intArrayOf(4, 6, 8, 12, 16, 24, 32)
    }

    private var index: Int = 0
        set(value) {
            field = value
            fractionString = "1/$snapLevel"
            tooltipText = fractionString
        }
    private val snapLevel: Int
        get() = snapLevels[index]
    private val snapFloat: Float
        get() = 1f / snapLevel
    private var fractionString: String = "1/$snapLevel"

    init {
        addLabel(TextLabel(palette, this, stage).apply {
            this.text = "editor.snap"
            this.isLocalizationKey = true
            this.textWrapping = false
            this.fontScaleMultiplier = 0.5f
            this.location.set(screenWidth = 0.35f)
        })
        addLabel(object : TextLabel<EditorScreen>(palette, this, stage) {
            override fun getRealText(): String {
                return fractionString
            }
        }.apply {
            this.textWrapping = false
            this.location.set(screenX = 0.35f, screenWidth = 0.65f)
        })
    }

    override var tooltipText: String? = fractionString
        get() = Localization["editor.snap.tooltip", field]

    private fun updateAndFlash() {
        editor.renderer.subbeatSection.setFlash(0.5f)
        editor.snap = snapFloat
        hoverTime = 0f
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)
        val minHover = 1f
        if (hoverTime > minHover && !wasClickedOn && editor.engine.playState == PlayState.STOPPED) {
            editor.renderer.subbeatSection.setFlash(Gdx.graphics.deltaTime)
        }
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        index = if (index + 1 >= snapLevels.size) 0 else index + 1
        updateAndFlash()
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        index = 0
        updateAndFlash()
    }
}