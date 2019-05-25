package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.graphics.Color


class InterpolatableColor() {

    val initial: Color = Color(1f, 1f, 1f, 1f)
    val current: Color = Color(1f, 1f, 1f, 1f)
    private val lerpBase: Color = Color(1f, 1f, 1f, 1f)
    private val lerpTarget: Color = Color(1f, 1f, 1f, 1f)
    
    constructor(init: Color) : this() {
        initial.set(init)
        current.set(init)
    }
    
    fun beginLerp(target: Color) {
        lerpBase.set(current)
        lerpTarget.set(target)
    }
    
    fun beginLerp(start: Color, end: Color) {
        lerpBase.set(start)
        lerpTarget.set(end)
    }
    
    fun reset() {
        current.set(initial)
    }

    /**
     * Call [beginLerp] prior to using this function
     */
    fun lerp(alpha: Float): Color {
        return current.set(lerpBase).lerp(lerpTarget, alpha)
    }

}