package io.github.chrislo27.bouncyroadmania.credits

import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.toolboks.i18n.Localization


object Credits {

    // TODO proper list eventually
    /*
    RHRE credits
    WaluigiTime64
    FrankPlanet
    meuol
     */

    private infix fun String.crediting(persons: String): Credit =
            Credit(this, persons)

    data class Credit(val type: String, val persons: String) {

        private val localization: String by lazy {
            "credits.title.$type"
        }
        private val isTitle by lazy { type == "title" }

        val text: String = if (isTitle) BRMania.TITLE else Localization[localization]

    }
    
}