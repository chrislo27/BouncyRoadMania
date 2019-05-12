package io.github.chrislo27.bouncyroadmania.editor.oopsies.impl

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.engine.event.Event


class EventMoveAction(val events: List<Event>, val oldPos: List<Rectangle>)
    : ReversibleAction<Editor> {

    private val newPos = events.map { Rectangle(it.bounds) }

    override fun redo(context: Editor) {
        events.forEachIndexed { i, it ->
            it.updateBounds {
                it.bounds.set(newPos[i])
            }
        }
        context.engine.recomputeCachedData()
    }

    override fun undo(context: Editor) {
        events.forEachIndexed { i, it ->
            it.updateBounds {
                it.bounds.set(oldPos[i])
            }
        }
        context.engine.recomputeCachedData()
    }

}

class EventPlaceAction(val events: List<Event>) : ReversibleAction<Editor> {

    override fun redo(context: Editor) {
        context.engine.addAllEvents(events)
        context.engine.recomputeCachedData()
    }

    override fun undo(context: Editor) {
        context.engine.removeAllEvents(events)
        context.engine.recomputeCachedData()
    }

}

class EventSelectionAction(val old: List<Event>, val new: List<Event>)
    : ReversibleAction<Editor> {

    override fun redo(context: Editor) {
        context.selection = new.toList()
    }

    override fun undo(context: Editor) {
        context.selection = old.toList()
    }
}

class EventRemoveAction(val events: List<Event>, val oldPos: List<Rectangle>)
    : ReversibleAction<Editor> {

    override fun undo(context: Editor) {
        context.engine.addAllEvents(events)
        events.forEachIndexed { index, entity ->
            entity.updateBounds {
                entity.bounds.set(oldPos[index])
            }
        }
        context.engine.recomputeCachedData()
    }

    override fun redo(context: Editor) {
        context.engine.removeAllEvents(events)
        context.engine.recomputeCachedData()
    }

}

