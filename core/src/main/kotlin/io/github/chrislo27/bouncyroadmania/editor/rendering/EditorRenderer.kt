package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.editor.*
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.util.RectanglePool
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class EditorRenderer(val editor: Editor) {

    companion object {
        private val TMP_MATRIX = Matrix4()
    }

    val main: BRManiaApp get() = editor.main
    val engine: Engine get() = editor.engine
    val theme: EditorTheme get() = editor.theme

    val trackCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, BRMania.WIDTH / Editor.EVENT_WIDTH, BRMania.HEIGHT / Editor.EVENT_HEIGHT)
        position.x = 0f
        position.y = calculateNormalCameraY()
        update()
    }
    val staticCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
        position.x = viewportWidth / 2
        position.y = viewportHeight / 2
        update()
    }
    val subbeatSection = SubbeatSection()
    var cameraPan: CameraPan? = null

    /**
     * Converts pixels to correct size for track camera width
     */
    fun toScaleX(float: Float): Float =
            (float / BRMania.WIDTH) * trackCamera.viewportWidth

    /**
     * Converts pixels to correct size for track camera height
     */
    fun toScaleY(float: Float): Float =
            (float / BRMania.HEIGHT) * trackCamera.viewportHeight

    fun getBeatRange(): IntRange =
            Math.round((trackCamera.position.x - trackCamera.viewportWidth / 2 * trackCamera.zoom) / toScaleX(
                    Editor.EVENT_WIDTH)) - 4..(Math.round(
                    (trackCamera.position.x + trackCamera.viewportWidth / 2 * trackCamera.zoom) / toScaleX(Editor.EVENT_WIDTH)) + 4)


    private fun calculateNormalCameraY(): Float = 1f + (engine.trackCount - Engine.MIN_TRACK_COUNT) / 10f * 3.5f

    fun render(batch: SpriteBatch) {
        val beatRange = getBeatRange()

        // update camera
        run {
            val transitionTime = Gdx.graphics.deltaTime / 0.15f
            val cameraYNormal = calculateNormalCameraY()
            val cameraZoomNormal = 1f + (engine.trackCount - Engine.MIN_TRACK_COUNT) / 10f
            val cameraY = cameraYNormal
            val cameraZoom = cameraZoomNormal
            trackCamera.position.y = MathUtils.lerp(trackCamera.position.y, cameraY, transitionTime)
            trackCamera.zoom = MathUtils.lerp(trackCamera.zoom, cameraZoom, transitionTime)

            val cameraPan = cameraPan
            if (cameraPan != null) {
                cameraPan.update(Gdx.graphics.deltaTime, trackCamera)
                if (cameraPan.done) {
                    this.cameraPan = null
                }
            }

            trackCamera.update()
        }

        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = staticCamera.combined
        batch.begin()
        batch.color = theme.background
        batch.fillRect(0f, 0f, staticCamera.viewportWidth, staticCamera.viewportHeight)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
        batch.projectionMatrix = TMP_MATRIX

        when (editor.editMode) {
            EditMode.ENGINE -> renderEngine(batch, beatRange)
            EditMode.EVENTS -> renderEvents(batch, beatRange)
            EditMode.PARAMETERS -> renderParams(batch, beatRange)
        }
    }

    private fun renderEvents(batch: SpriteBatch, beatRange: IntRange) {
        val beatRangeStartFloat = beatRange.first.toFloat()
        val beatRangeEndFloat = beatRange.last.toFloat()

        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = trackCamera.combined
        batch.begin()

        val trackYOffset = toScaleY(-Editor.TRACK_LINE_THICKNESS / 2f)
        val font = main.defaultFont
        font.scaleFont(trackCamera)

        this.renderHorizontalTrackLines(batch, beatRangeStartFloat, beatRangeEndFloat - beatRangeStartFloat, trackYOffset)

        // events
        val events = engine.events
        events.forEach {
            it.updateInterpolation(false)
        }
        if (editor.selection.isNotEmpty()) {
            val clickOccupation = editor.clickOccupation
            if (clickOccupation is ClickOccupation.SelectionDrag) {
                val oldColor = batch.packedColor
                val rect = RectanglePool.obtain()
                rect.set(clickOccupation.lerpLeft, clickOccupation.lerpBottom, clickOccupation.lerpRight - clickOccupation.lerpLeft, clickOccupation.lerpTop - clickOccupation.lerpBottom)

                batch.color = theme.selection.fill
                batch.fillRect(rect)
                batch.color = theme.selection.border
                batch.drawRect(rect, toScaleX(Editor.SELECTION_BORDER), toScaleY(Editor.SELECTION_BORDER))

                batch.packedColor = oldColor
                RectanglePool.free(rect)
            }
        }
        engine.events.forEach {
            if (it.inRenderRange(beatRangeStartFloat, beatRangeEndFloat)) {
                it.render(batch, this.editor)
            }
        }
        // Stripe board in invalid positions
        if (editor.selection.isNotEmpty()) {
            this.renderStripeBoard(batch, main.shapeRenderer)
        }

        // beat lines
        this.renderBeatLines(batch, beatRange, trackYOffset, true)

        // beat numbers
        this.renderBeatNumbers(batch, beatRange, font)

        // top trackers
        this.renderTopTrackers(batch, beatRange, trackYOffset)

        // time signatures
        this.renderTimeSignatures(batch, beatRange)

        // bottom trackers
        this.renderBottomTrackers(batch, beatRange)

        main.shapeRenderer.projectionMatrix = trackCamera.combined
        this.renderOtherUI(batch, main.shapeRenderer, beatRange, font)
        main.shapeRenderer.projectionMatrix = main.defaultCamera.combined

        font.unscaleFont()
        batch.end()
        batch.projectionMatrix = TMP_MATRIX
    }

    private fun renderEngine(batch: SpriteBatch, beatRange: IntRange) {
        engine.render(batch)
    }

    private fun renderParams(batch: SpriteBatch, beatRange: IntRange) {
//        TMP_MATRIX.set(batch.projectionMatrix)
//        batch.projectionMatrix = staticCamera.combined
//        batch.begin()
//        with(engine) {
//            if (gradientDirection == GradientDirection.VERTICAL) {
//                batch.drawQuad(0f, 0f, gradientStart, staticCamera.viewportWidth, 0f, gradientStart, staticCamera.viewportWidth, staticCamera.viewportHeight, gradientEnd, 0f, staticCamera.viewportHeight, gradientEnd)
//            } else {
//                batch.drawQuad(0f, 0f, gradientStart, staticCamera.viewportWidth, 0f, gradientEnd, staticCamera.viewportWidth, staticCamera.viewportHeight, gradientEnd, 0f, staticCamera.viewportHeight, gradientStart)
//            }
//        }
//        batch.end()
//        batch.projectionMatrix = TMP_MATRIX
        val lastX = engine.camera.position.x
        engine.camera.position.x -= 56f
        engine.camera.update()
        engine.render(batch)
        engine.camera.position.x = lastX
        engine.camera.update()
    }

    fun getDebugString(): String {
        return ""
    }

}