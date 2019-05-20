package io.github.chrislo27.bouncyroadmania.engine


data class TextBox(val text: String, var requiresInput: Boolean, var secsBeforeCanInput: Float = 0.5f)