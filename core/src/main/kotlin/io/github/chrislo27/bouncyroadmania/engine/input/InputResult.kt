package io.github.chrislo27.bouncyroadmania.engine.input

import io.github.chrislo27.bouncyroadmania.engine.Engine
import kotlin.math.absoluteValue


data class InputResult(val type: InputType, val accuracySec: Float, val accuracyPercent: Float) {
    val inputScore: InputScore = run {
        val p = accuracySec.absoluteValue
        when {
            p <= Engine.ACE_OFFSET -> InputScore.ACE
            p <= Engine.GOOD_OFFSET -> InputScore.GOOD
            p <= Engine.BARELY_OFFSET -> InputScore.BARELY
            else -> InputScore.MISS
        }
    }
}