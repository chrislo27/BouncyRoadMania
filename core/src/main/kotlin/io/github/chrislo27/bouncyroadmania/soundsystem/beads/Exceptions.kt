package io.github.chrislo27.bouncyroadmania.soundsystem.beads

import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.MemoryUtils


abstract class MusicLoadingException(message: String) : RuntimeException(message) {
    abstract fun getLocalizedText(): String
}

class MusicTooLargeException(val bytes: Long, val original: OutOfMemoryError)
    : MusicLoadingException("Music too large - ${bytes / (1024 * 1024)} MB / ${MemoryUtils.maxMemory / 1024} MB") {
    override fun getLocalizedText(): String {
        return Localization["musicSelect.tooBig",
                bytes / (1024 * 1024),
                MemoryUtils.maxMemory / 1024]
    }
}

class MusicWayTooLargeException(val bytes: Long)
    : MusicLoadingException("Music too large, max ${Int.MAX_VALUE} bytes (was $bytes bytes)") {
    override fun getLocalizedText(): String {
        return Localization["musicSelect.wayTooBig", Int.MAX_VALUE]
    }
}
