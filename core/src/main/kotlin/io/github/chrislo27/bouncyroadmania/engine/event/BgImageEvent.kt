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

    enum class RenderType(val localization: String, val equivalent: ImageLabel.ImageRendering) {
        FILL("bgImageEvent.renderType.fill", ImageLabel.ImageRendering.RENDER_FULL),
        SCALE_TO_FIT("bgImageEvent.renderType.scaleToFit", ImageLabel.ImageRendering.ASPECT_RATIO),
        EXPAND_TO_FIT("bgImageEvent.renderType.expandToFit", ImageLabel.ImageRendering.IMAGE_ASPECT_RATIO);

        companion object {
            val VALUES = values().toList()
        }
    }

    override val canBeCopied: Boolean = true
    override val isStretchable: Boolean = true
    override val hasEditableParams: Boolean = true
    override val shouldAlwaysBeSimulated: Boolean = true

    var textureHash: String? = null
    var renderType: RenderType = RenderType.SCALE_TO_FIT

    init {
        bounds.width = 1f
    }

    override fun copy(): BgImageEvent {
        return BgImageEvent(engine, instantiator).also {
            it.bounds.set(this.bounds)
            it.updateInterpolation(true)
            it.textureHash = this.textureHash
        }
    }

    fun getImageAlpha(): Float {
        var transitionDur = 1f
        if (bounds.width < transitionDur * 2) {
            transitionDur = bounds.width / 2f
        }
        val t = transitionDur / bounds.width

        val progress = (engine.beat - bounds.x) / bounds.width
        return if (progress !in 0f..1f) 0f else if (progress < t) (progress / t) else if (progress > 1f - t) (1f - (progress - (1f - t)) / t) else 1f
    }

    override fun createParamsStage(editor: Editor, stage: EditorStage): BgImageEventParamsStage {
        return BgImageEventParamsStage(stage)
    }

    override fun fromJson(node: ObjectNode) {
        super.fromJson(node)
        textureHash = node["textureHash"]?.asText()
        val renderTypeStr = node["renderType"]?.asText()
        if (renderTypeStr != null) {
            renderType = RenderType.VALUES.find { it.name == renderTypeStr } ?: RenderType.SCALE_TO_FIT
        }
    }

    override fun toJson(node: ObjectNode) {
        super.toJson(node)
        if (textureHash != null) {
            node.put("textureHash", textureHash)
        }
        node.put("renderType", renderType.name)
    }

    inner class BgImageEventParamsStage(parent: EditorStage) : EventParamsStage<BgImageEvent>(parent, this@BgImageEvent) {
        val img = ImageLabel(palette, contentStage, contentStage).apply {
            val tex = engine.textures[textureHash]
            this.image = if (tex != null) TextureRegion(tex) else null
            this.renderType = this@BgImageEvent.renderType.equivalent
            this.location.set(screenHeight = 0.623077f)
            this.location.set(screenY = 0.9f - this.location.screenHeight)
        }
        val label = TextLabel(palette, contentStage, contentStage).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.location.set(img.location)
        }

        init {
            contentStage.elements += img
            contentStage.elements += label
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
                                        label.background = false
                                        menu.removeSelf()
                                        persistDirectory(PreferenceKeys.FILE_CHOOSER_EDITOR_TEX, file.parentFile)
                                        engine.recomputeCachedData()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Gdx.app.postRunnable {
                                        label.text = Localization["bgImageEvent.failedToLoad"]
                                        label.background = true
                                        menu.removeSelf()
                                    }
                                }
                            }
                        } else {
                            Gdx.app.postRunnable {
                                label.text = ""
                                label.background = false
                                menu.removeSelf()
                            }
                        }
                    }
                }
            }
            contentStage.elements += TextLabel(palette, contentStage, contentStage).apply {
                this.location.set(screenHeight = 0.1f, screenWidth = 0.5f, pixelWidth = -4f)
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "bgImageEvent.renderType"
            }
            contentStage.elements += Button(palette, contentStage, contentStage).apply {
                this.location.set(screenHeight = 0.1f, screenX = 0.5f, screenWidth = 0.5f)
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.text = renderType.localization
                    this.fontScaleMultiplier = 0.9f
                })
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = renderType.localization + ".tooltip"
                fun cycle(dir: Int) {
                    val current = renderType
                    val values = RenderType.VALUES
                    val nextIndex = values.indexOf(current) + dir.coerceIn(-1, 1)
                    val next: RenderType = if (nextIndex < 0) values.last() else if (nextIndex >= values.size) values.first() else values[nextIndex]
                    renderType = next
                    img.renderType = next.equivalent
                    this.tooltipText = next.localization + ".tooltip"
                    (labels.first() as TextLabel).text = next.localization
                }
                this.leftClickAction = { _, _ ->
                    cycle(1)
                }
                this.rightClickAction = { _, _ ->
                    cycle(-1)
                }
            }
            
            this.updatePositions()
        }
    }

}