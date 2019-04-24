package io.github.chrislo27.bouncyroadmania.util

import kotlin.reflect.KProperty


data class Position(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {

    // The delegate operator functions are designed for PaperRenderable

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return when (val name = property.name) {
            "posX" -> x
            "posY" -> y
            "posZ" -> z
            else -> error("Unsupported property name $name in delegate")
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        when (val name = property.name) {
            "posX" -> x = value
            "posY" -> y = value
            "posZ" -> z = value
            else -> error("Unsupported property name $name in delegate")
        }
    }

}
