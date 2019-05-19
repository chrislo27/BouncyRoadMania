package io.github.chrislo27.bouncyroadmania.engine.input


enum class InputScore(val weight: Float) {
    ACE(1.0f), GOOD(0.9f), BARELY(0.7f), MISS(0f)
}