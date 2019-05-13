package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.bouncyroadmania.soundsystem.Music
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSoundSystem
import java.io.InputStream

class MusicData(handle: FileHandle, val engine: Engine) : Disposable {

    val music: Music = BeadsSoundSystem.newMusic(handle)
    private val reader: InputStream = handle.read()

    override fun dispose() {
        music.dispose()
        StreamUtils.closeQuietly(reader)
    }
}