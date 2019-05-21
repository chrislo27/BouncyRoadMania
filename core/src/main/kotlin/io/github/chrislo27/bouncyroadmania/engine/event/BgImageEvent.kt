package io.github.chrislo27.bouncyroadmania.engine.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.stage.EditorStage
import io.github.chrislo27.bouncyroadmania.editor.stage.EventParamsStage
import io.github.chrislo27.bouncyroadmania.editor.stage.MenuOverlay
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.registry.Instantiator
import io.github.chrislo27.bouncyroadmania.util.TinyFDWrapper
import io.github.chrislo27.bouncyroadmania.util.attemptRememberDirectory
import io.github.chrislo27.bouncyroadmania.util.persistDirectory
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextLabel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest


class BgImageEvent(engine: Engine, instantiator: Instantiator) : InstantiatedEvent(engine, instantiator) {

    override val canBeCopied: Boolean = true
    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    override val shouldAlwaysBeSimulated: Boolean = true

    var textureHash: String? = null

    override fun copy(): BgImageEvent {
        return BgImageEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.textureHash = this.textureHash
        }
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): BgImageEventParamsStage {
        return BgImageEventParamsStage(stage)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        textureHash = node["textureHash"]?.asText()
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        if (textureHash != null) {
            node.put("textureHash", textureHash)
        }
    }

    inner class BgImageEventParamsStage(parent: EditorStage) : EventParamsStage<BgImageEvent>(parent, this@BgImageEvent) {
        val img = ImageLabel(palette, contentStage, contentStage).apply {
            val tex = engine.textures[textureHash]
            this.image = if (tex != null) TextureRegion(tex) else null
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenY = 0.1f, screenHeight = 0.8f)
        }
        val label = TextLabel(palette, contentStage, contentStage).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.location.set(screenY = 0f, screenHeight = 0.1f)
        }

        init {
            contentStage.elements += Button(palette, contentStage, contentStage).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.text = "bgImageEvent.select"
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.fontScaleMultiplier = 0.9f
                })
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "bgImageEvent.tooltip"
                this.location.set(screenY = 0.9f, screenHeight = 0.1f)
                this.leftClickAction = { _, _ ->
                    val menu = MenuOverlay(parent.editor, parent, palette).apply {
                        elements += ColourPane(this, this).apply {
                            this.colour.set(0f, 0f, 0f, 0.75f)
                        }
                        elements += TextLabel(palette, this, this).apply {
                            this.text = "closeChooser"
                            this.isLocalizationKey = true
                        }
                    }
                    parent.elements += menu
                    parent.updatePositions()
                    GlobalScope.launch {
                        val filter = TinyFDWrapper.Filter(listOf("*.png", "*.jpg", "*.bmp"), Localization["bgImageEvent.fileFilter"] + " (*.png, *.jpg, *.bmp)")
                        val file = TinyFDWrapper.openFile(Localization["bgImageEvent.fileChooserTitle"],
                                attemptRememberDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_TEX)?.absolutePath?.plus("/"), false, filter)
                        if (file != null && file.exists()) {
                            Gdx.app.postRunnable {
                                try {
                                    val texture = Texture(FileHandle(file))
                                    val hash: String = MessageDigest.getInstance("SHA-1").let {
                                        it.update(file.readBytes())
                                        BigInteger(1, it.digest()).toString(16)
                                    }
                                    if (engine.textures.containsKey(hash)) {
                                        texture.dispose()
                                    } else {
                                        (engine.textures as MutableMap)[hash] = texture
                                    }
                                    this@BgImageEvent.textureHash = hash
                                    Gdx.app.postRunnable {
                                        img.image = TextureRegion(texture)
                                        label.text = ""
                                        menu.removeSelf()
                                        persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_TEX, file.parentFile)
                                        engine.recomputeCachedData()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Gdx.app.postRunnable {
                                        label.text = Localization["bgImageEvent.failedToLoad"]
                                        menu.removeSelf()
                                    }
                                }
                            }
                        } else {
                            Gdx.app.postRunnable {
                                label.text = ""
                                menu.removeSelf()
                            }
                        }
                    }
                }
            }
            contentStage.elements += img
            contentStage.elements += label
        }
    }

}