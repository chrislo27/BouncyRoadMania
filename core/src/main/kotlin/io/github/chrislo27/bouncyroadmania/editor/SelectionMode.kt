package io.github.chrislo27.bouncyroadmania.editor

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.toolboks.util.gdxutils.intersects


enum class SelectionMode {

    REPLACE {
        override fun createNewSelection(entities: List<Event>, existingSelection: List<Event>, selection: Rectangle): List<Event> {
            return getNewCaptured(entities, selection)
        }

        override fun wouldEventBeIncluded(entity: Event, selection: Rectangle, entities: List<Event>, existingSelection: List<Event>): Boolean {
            return entity.bounds.intersects(selection)
        }
    },
    INVERT {
        override fun createNewSelection(entities: List<Event>, existingSelection: List<Event>, selection: Rectangle): List<Event> {
            return mutableListOf<Event>().also { list ->
                list.addAll(existingSelection)
                getNewCaptured(entities, selection).forEach {
                    if (it in list) {
                        list -= it
                    } else {
                        list += it
                    }
                }
            }
        }

        override fun wouldEventBeIncluded(entity: Event, selection: Rectangle, entities: List<Event>, existingSelection: List<Event>): Boolean {
            return entity.bounds.intersects(selection) xor (entity in existingSelection)
        }
    },
    ADD {
        override fun createNewSelection(entities: List<Event>, existingSelection: List<Event>, selection: Rectangle): List<Event> {
            return (existingSelection.toList() + getNewCaptured(entities, selection)).distinct()
        }

        override fun wouldEventBeIncluded(entity: Event, selection: Rectangle, entities: List<Event>, existingSelection: List<Event>): Boolean {
            return entity.bounds.intersects(selection) || entity in existingSelection
        }
    };

    abstract fun createNewSelection(entities: List<Event>, existingSelection: List<Event>, selection: Rectangle): List<Event>
    abstract fun wouldEventBeIncluded(entity: Event, selection: Rectangle, entities: List<Event>, existingSelection: List<Event>): Boolean

    protected fun getNewCaptured(entities: List<Event>, selection: Rectangle): List<Event> =
            entities.filter { it.bounds.intersects(selection) }

}