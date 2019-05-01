package io.github.chrislo27.bouncyroadmania.util


object MusicCredit {
    fun credit(vararg songTitle: String): String {
        return """${songTitle.joinToString(separator = ", ") { "\"$it\""}} Kevin MacLeod (incompetech.com)
Licensed under Creative Commons: By Attribution 3.0 License
http://creativecommons.org/licenses/by/3.0/"""
    }
}