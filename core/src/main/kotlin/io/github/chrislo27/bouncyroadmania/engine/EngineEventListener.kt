package io.github.chrislo27.bouncyroadmania.engine

import io.github.chrislo27.bouncyroadmania.engine.event.Event


interface EngineEventListener {
    
    fun onPlayStateChanged(oldState: PlayState, newState: PlayState)
    
    fun onEventAdded(event: Event)
    
    fun onEventRemoved(event: Event)
    
}