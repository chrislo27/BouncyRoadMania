package io.github.chrislo27.bouncyroadmania.engine.input


enum class InputScore(val weight: Float) {
    ACE(1.0f), GOOD(0.85f), BARELY(0.6f), MISS(0f)
}