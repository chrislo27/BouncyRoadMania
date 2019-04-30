package io.github.chrislo27.bouncyroadmania.soundsystem.beads

import io.github.chrislo27.toolboks.util.MemoryUtils


abstract class MusicLoadingException(message: String) : RuntimeException(message)

class MusicTooLargeException(val bytes: Long, val original: OutOfMemoryError) : MusicLoadingException("Music too large - ${bytes / (1024 * 1024)} MB / ${MemoryUtils.maxMemory / 1024} MB")

class MusicWayTooLargeException(val bytes: Long) : MusicLoadingException("Music too large, max ${Int.MAX_VALUE} bytes (was $bytes bytes)")
