package io.github.chrislo27.bouncyroadmania.engine

import io.github.chrislo27.bouncyroadmania.renderer.PaperRenderable
import io.github.chrislo27.bouncyroadmania.util.Position


abstract class Entity(val engine: Engine) : PaperRenderable {

    val position: Position = Position(0f, 0f, 0f)
    var kill: Boolean = false

    override var posX: Float by position
    override var posY: Float by position
    override var posZ: Float by position

    open fun renderUpdate(delta: Float) {

    }

    open fun onPlayStateChanged(oldValue: PlayState, newValue: PlayState) {}

}