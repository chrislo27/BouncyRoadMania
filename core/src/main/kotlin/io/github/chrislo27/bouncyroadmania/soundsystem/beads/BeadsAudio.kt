package io.github.chrislo27.bouncyroadmania.soundsystem.beads

import com.badlogic.gdx.Gdx.net
import net.beadsproject.beads.data.Sample


open class BeadsAudio(channels: Int, sampleRate: Int) {

    val sample: Sample = Sample(0.0, channels, sampleRate.toFloat()).apply {
        clear()
    }

}