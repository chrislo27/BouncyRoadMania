package io.github.chrislo27.bouncyroadmania.editor.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.bouncyroadmania.editor.ClickOccupation
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.util.scaleFont
import io.github.chrislo27.bouncyroadmania.util.unscaleFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul


fun EditorRenderer.renderTopTrackers(batch: SpriteBatch, beatRange: IntRange, trackYOffset: Float) {
    // trackers (playback start, music)
    val borderedFont = main.defaultBorderedFont
    val oldFontColor = borderedFont.color

    fun getTrackerTime(time: Float, noBeat: Boolean = false): String {
        val signedSec = if (noBeat) time else engine.tempos.beatsToSeconds(time)
        val sec = Math.abs(signedSec)
        val seconds = (if (signedSec < 0) "-" else "") +
                Editor.TRACKER_MINUTES_FORMATTER.format((sec / 60).toLong()) + ":" + Editor.TRACKER_TIME_FORMATTER.format(sec % 60.0)
        if (noBeat) {
            return seconds
        }
        return Localization["tracker.any.time",
                Editor.THREE_DECIMAL_PLACES_FORMATTER.format(time.toDouble()), seconds]
    }

    fun renderAboveTracker(text: String?, controlText: String?, units: Int, beat: Float, color: Color,
                           trackerTime: String = getTrackerTime(beat),
                           triangleHeight: Float = 0.4f, bpmText: String? = null, showMusicUnsnap: Boolean = false) {
        val triangleWidth = toScaleX(triangleHeight * Editor.EVENT_HEIGHT)
        val x = beat - toScaleX(Editor.TRACK_LINE_THICKNESS * 1.5f) / 2
        val y = trackYOffset
        val height = (engine.trackCount + 1.25f + 1.2f * units) + toScaleY(Editor.TRACK_LINE_THICKNESS)
        batch.packedColor = color.toFloatBits()
        batch.fillRect(x, y, toScaleX(Editor.TRACK_LINE_THICKNESS * 1.5f),
                       height - triangleHeight / 2)
        batch.draw(AssetRegistry.get<Texture>("tracker_right_tri"),
                   x, y + height - triangleHeight, triangleWidth, triangleHeight)

        borderedFont.scaleFont(trackCamera)
        borderedFont.scaleMul(0.75f)
        borderedFont.color = batch.color
        if (text != null) {
            borderedFont.drawCompressed(batch, text, x - 1.05f, y + height, 1f, Align.right)
        }
        borderedFont.drawCompressed(batch, trackerTime, x + triangleWidth + 0.025f, y + height, 1f, Align.left)
        if (bpmText != null) {
            borderedFont.drawCompressed(batch, bpmText,
                                        x + triangleWidth + 0.025f,
                                        y + height + borderedFont.capHeight * 1.25f,
                                        1f, Align.left)
        }

        val line = borderedFont.lineHeight

        if (controlText != null) {
            borderedFont.scaleMul(0.75f)
            borderedFont.drawCompressed(batch, controlText, x - 1.05f, y + height - line, 1f,
                                        Align.right)
        }

        if (showMusicUnsnap) {
//                    borderedFont.scaleMul(0.75f)
            borderedFont.drawCompressed(batch, Localization["tracker.music.unsnap"], x + 0.05f, y + height - line, 1f,
                                        Align.left)
        }

        borderedFont.scaleFont(trackCamera)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    if (editor.cachedPlaybackStart.first != engine.tempos.beatsToSeconds(engine.playbackStart)) {
        editor.cachedPlaybackStart = engine.tempos.beatsToSeconds(engine.playbackStart) to getTrackerTime(
                engine.playbackStart)
    }
    if (editor.cachedMusicStart.first != engine.musicStartSec) {
        editor.cachedMusicStart = engine.musicStartSec to getTrackerTime(engine.musicStartSec, noBeat = true)
    }

    renderAboveTracker(Localization["tracker.music"], Localization["tracker.music.controls"],
                       1, engine.musicStartSec, theme.trackers.musicStart,
            editor.cachedMusicStart.second, showMusicUnsnap = editor.clickOccupation is ClickOccupation.Music)
    renderAboveTracker(Localization["tracker.playback"], Localization["tracker.playback.controls"],
                       0, engine.playbackStart, theme.trackers.playback, editor.cachedPlaybackStart.second)

    if (engine.playState != PlayState.STOPPED) {
        val position = engine.beat
        renderAboveTracker(null, null, 0, position,
                           theme.trackers.playback, triangleHeight = 0f,
                           bpmText = "♩=${Editor.ONE_DECIMAL_PLACE_FORMATTER.format(
                                   engine.tempos.tempoAt(engine.beat))}")
    }

    borderedFont.color = oldFontColor
    borderedFont.unscaleFont()
}
