package io.github.chrislo27.bouncyroadmania.util

import com.badlogic.gdx.graphics.Color


fun Color.toJsonString(): String = "#${if (this.a >= 1f) this.toString().take(6) else this.toString()}"

fun Color.fromJsonString(str: String?) = try {
    this.set(Color.valueOf(str))
} catch (ignored: Exception) {
}