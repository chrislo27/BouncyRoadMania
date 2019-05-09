package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode


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

    val background: Color = Color(1f, 1f, 1f, 1f)
    val trackLine: Color = Color(0f, 0f, 0f, 1f)

    fun toTree(node: ObjectNode) {
        node.put("background", background.toJsonString())
        node.put("trackLine", trackLine.toJsonString())
    }

    fun fromTree(node: ObjectNode) {
        background.fromJsonString(node["background"]?.asText(""))
        trackLine.fromJsonString(node["trackLine"]?.asText(""))
    }

}