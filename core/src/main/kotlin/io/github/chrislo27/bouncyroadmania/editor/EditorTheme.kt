package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker
import io.github.chrislo27.bouncyroadmania.engine.tracker.musicvolume.MusicVolumeChange
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange


class EditorTheme {

    companion object {
        val DEFAULT_THEMES: LinkedHashMap<String, EditorTheme> = linkedMapOf(
                "light" to EditorTheme().apply {
                    background.set(0.925f, 0.925f, 0.925f, 1f)
                    trackLine.set(0.1f, 0.1f, 0.1f, 1f)
                },
                "dark" to EditorTheme().apply {
                    background.set(0.15f, 0.15f, 0.15f, 1f)
                    trackLine.set(0.95f, 0.95f, 0.95f, 1f)
                }
        )

        private fun Color.toJsonString(): String = "#${if (this.a >= 1f) this.toString().take(6) else this.toString()}"
        private fun Color.fromJsonString(str: String?) = try {
            this.set(Color.valueOf(str))
        } catch (ignored: Exception) {
        }
    }

    class TrackersGroup {
        val playback: Color = Color(0f, 1f, 0f, 1f)
        val musicStart: Color = Color(1f, 0f, 0f, 1f)
        val musicVolume: Color = Color(1f, 0.4f, 0f, 1f)
        val tempoChange: Color = Color(0.4f, 0.4f, 0.9f, 1f)
    }

    val background: Color = Color(1f, 1f, 1f, 1f)
    val trackLine: Color = Color(0f, 0f, 0f, 1f)
    val trackers: TrackersGroup = TrackersGroup()

    fun getTrackerColour(tracker: Tracker<*>?): Color {
        return when (tracker) {
            is TempoChange -> trackers.tempoChange
            is MusicVolumeChange -> trackers.musicVolume
            else -> Color.WHITE
        }
    }

    fun toTree(node: ObjectNode) {
        node.put("background", background.toJsonString())
        node.put("trackLine", trackLine.toJsonString())

        node.putObject("trackers").apply {
            put("playback", trackers.playback.toJsonString())
            put("musicStart", trackers.musicStart.toJsonString())
            put("musicVolume", trackers.musicVolume.toJsonString())
            put("tempoChange", trackers.tempoChange.toJsonString())
        }
    }

    fun fromTree(node: ObjectNode) {
        background.fromJsonString(node["background"]?.asText(""))
        trackLine.fromJsonString(node["trackLine"]?.asText(""))

        (node["trackers"] as? ObjectNode)?.also { n ->
            trackers.playback.fromJsonString(n["playback"]?.asText(""))
            trackers.musicStart.fromJsonString(n["musicStart"]?.asText(""))
            trackers.musicVolume.fromJsonString(n["musicVolume"]?.asText(""))
            trackers.tempoChange.fromJsonString(n["tempoChange"]?.asText(""))
        }
    }

}