package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper

class MetronomeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                      stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    private val metronomeFrames: List<TextureRegion> by lazy {
        val tex = AssetRegistry.get<Texture>("ui_metronome")
        val size = 64
        // center, right, rightmost, right, center, left, leftmost, left
        listOf(
                TextureRegion(tex, size * 2, 0, size, size),
                TextureRegion(tex, size * 3, 0, size, size),
                TextureRegion(tex, size * 4, 0, size, size),
                TextureRegion(tex, size * 3, 0, size, size),
                TextureRegion(tex, size * 2, 0, size, size),
                TextureRegion(tex, size, 0, size, size),
                TextureRegion(tex, 0, 0, size, size),
                TextureRegion(tex, size, 0, size, size)
        )
    }
    private var start = 0L
    private val label = ImageLabel(palette, this, stage).apply {
        this.image = metronomeFrames[0]
    }

    init {
        addLabel(label)
        tooltipTextIsLocalizationKey = true
        tooltipText = "editor.metronome"
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val remix = editor.engine
        if (remix.metronome) {
            val beat = remix.beat
            val time = if (remix.playState == PlayState.PLAYING) {
                ((beat / (remix.timeSignatures.getTimeSignature(beat)?.noteFraction ?: 1f)) % 2 / 2)
            } else MathHelper.getSawtoothWave(1.25f)
            label.image = metronomeFrames[(time * metronomeFrames.size).toInt().coerceIn(0, metronomeFrames.size - 1)]
        } else {
            label.image = metronomeFrames[0]
        }
        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        start = System.currentTimeMillis()
        editor.engine.metronome = !editor.engine.metronome
    }
}