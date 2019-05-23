package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker
import io.github.chrislo27.bouncyroadmania.engine.tracker.musicvolume.MusicVolumeChange
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.util.fromJsonString
import io.github.chrislo27.bouncyroadmania.util.toJsonString


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
                    events.generic.set(0.65f, 0.65f, 0.65f, 1f)
                }
        )
    }

    class TrackersGroup {
        val playback: Color = Color(0f, 1f, 0f, 1f)
        val musicStart: Color = Color(1f, 0f, 0f, 1f)
        val musicVolume: Color = Color(1f, 0.4f, 0f, 1f)
        val tempoChange: Color = Color(0.4f, 0.4f, 0.9f, 1f)
    }

    class EventsGroup {
        val selectionTint: Color = Color(0f, 0.75f, 0.75f, 1f)
        val nameText: Color = Color(0f, 0f, 0f, 1f)

        val generic: Color = Color(0.85f, 0.85f, 0.85f, 1f)
        val input: Color = Color(1f, 178f / 255f, 191f / 255f, 1f)
        val skillStar: Color = Color(1f, 226f / 255f, 124f / 255f, 1f)
    }

    class SelectionGroup {
        val fill: Color = Color(0.1f, 0.75f, 0.75f, 0.333f)
        val border: Color = Color(0.1f, 0.85f, 0.85f, 1f)
    }

    val background: Color = Color(1f, 1f, 1f, 1f)
    val trackLine: Color = Color(0f, 0f, 0f, 1f)
    val trackers: TrackersGroup = TrackersGroup()
    val events: EventsGroup = EventsGroup()
    val selection: SelectionGroup = SelectionGroup()

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

        node.putObject("events").apply {
            put("generic", events.generic.toJsonString())
            put("input", events.input.toJsonString())
            put("skillStar", events.skillStar.toJsonString())
            put("selectionTint", events.selectionTint.toJsonString())
            put("nameText", events.nameText.toJsonString())
        }

        node.putObject("selection").apply {
            put("fill", selection.fill.toJsonString())
            put("border", selection.border.toJsonString())
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

        (node["events"] as? ObjectNode)?.also { n ->
            events.generic.fromJsonString(n["generic"]?.asText(""))
            events.input.fromJsonString(n["input"]?.asText(""))
            events.skillStar.fromJsonString(n["skillStar"]?.asText(""))
            events.selectionTint.fromJsonString(n["selectionTint"]?.asText(""))
            events.nameText.fromJsonString(n["nameText"]?.asText(""))
        }

        (node["selection"] as? ObjectNode)?.also { n ->
            selection.fill.fromJsonString(n["fill"]?.asText(""))
            selection.border.fromJsonString(n["border"]?.asText(""))
        }
    }

}