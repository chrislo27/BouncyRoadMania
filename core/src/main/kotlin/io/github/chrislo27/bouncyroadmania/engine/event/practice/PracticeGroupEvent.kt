package io.github.chrislo27.bouncyroadmania.engine.event.practice

import com.badlogic.gdx.Gdx
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.event.DeployEvent
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.engine.input.InputScore
import io.github.chrislo27.toolboks.util.gdxutils.maxX


/**
 * This event will keep copying itself at the end of its bounds if [Engine.xMoreTimes] is greater than zero.
 * All events it generates through [groupFactory] are managed, and will be deleted along with this event
 * at the end of the bounds.
 */
class PracticeGroupEvent(engine: Engine,
                         val groupFactory: PracticeGroupEvent.(offset: Float) -> List<Event>,
                         val onEnd: PracticeGroupEvent.(Engine) -> Unit,
                         val copyOffset: Float = 0f)
    : Event(engine) {

    override val canBeCopied: Boolean = true
    private val managedEvents: MutableList<Event> = mutableListOf()
    private var satisfied = false

    override fun copy(): PracticeGroupEvent {
        return PracticeGroupEvent(engine, groupFactory, onEnd, copyOffset).also {
            it.bounds.width = this.bounds.width
        }
    }

    private fun checkSatisfaction() {
        // Check satisfaction for xMoreTimes
        if (!satisfied && engine.xMoreTimes > 0 && engine.inputResults.count { it.inputScore != InputScore.MISS } >= managedEvents.count { it is DeployEvent } * 2 && engine.inputResults.size > 0) {
            satisfied = true
        }
    }

    override fun onStart() {
        val created = groupFactory.invoke(this, this.bounds.x)
        managedEvents.clear()
        managedEvents.addAll(created)
        Gdx.app.postRunnable {
            engine.addAllEvents(managedEvents)
        }
    }

    override fun whilePlaying() {
        checkSatisfaction()
    }

    override fun onEnd() {
        checkSatisfaction()
        if (satisfied && engine.xMoreTimes > 0) {
            engine.xMoreTimes--
        }
        engine.resetInputs()

        if (engine.xMoreTimes > 0) {
            // Copy self
            Gdx.app.postRunnable {
                engine.addEvent(this.copy().also {
                    it.bounds.x = this.bounds.maxX + copyOffset
                })
            }
        } else {
            Gdx.app.postRunnable {
                onEnd(engine)
            }
        }

        // Delete self
        Gdx.app.postRunnable {
            engine.removeAllEvents(managedEvents)
            engine.removeEvent(this)
        }
    }
}