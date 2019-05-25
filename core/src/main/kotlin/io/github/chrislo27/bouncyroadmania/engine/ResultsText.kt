package io.github.chrislo27.bouncyroadmania.engine

import io.github.chrislo27.toolboks.i18n.Localization


data class ResultsText(val title: String, val ok: String, val firstPositive: String, val firstNegative: String, val secondPositive: String, val secondNegative: String) {
    
    constructor() : this(Localization["results.default.title"], Localization["results.default.ok"], Localization["results.default.first.positive"], Localization["results.default.first.negative"], Localization["results.default.second.positive"], Localization["results.default.second.negative"])
    
}