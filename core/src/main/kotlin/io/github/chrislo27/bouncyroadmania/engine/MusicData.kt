package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.bouncyroadmania.soundsystem.Music
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSoundSystem
import java.io.InputStream

class MusicData(val handle: FileHandle, val engine: Engine, progressListener: (Float) -> Unit = {}) : Disposable {

    val music: Music = BeadsSoundSystem.newMusic(handle, progressListener)
    private val reader: InputStream = handle.read()

    override fun dispose() {
        music.dispose()
        StreamUtils.closeQuietly(reader)
    }
}