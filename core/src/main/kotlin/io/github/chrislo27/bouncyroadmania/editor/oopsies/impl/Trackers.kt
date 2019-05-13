package io.github.chrislo27.bouncyroadmania.editor.oopsies.impl

import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.editor.oopsies.ReversibleAction
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignature
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignatures
import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker
import io.github.chrislo27.bouncyroadmania.engine.tracker.TrackerContainer


class TrackerAction(val tracker: Tracker<*>, val remove: Boolean) : ReversibleAction<Editor> {

    private val container: TrackerContainer<*> = tracker.container

    override fun redo(context: Editor) {
        if (remove) {
            container.remove(tracker)
        } else {
            container.add(tracker)
        }
    }

    override fun undo(context: Editor) {
        if (!remove) {
            container.remove(tracker)
        } else {
            container.add(tracker)
        }
    }
}

class TrackerValueChange(val original: Tracker<*>, var current: Tracker<*>)
    : ReversibleAction<Editor> {

    private val container = original.container

    override fun redo(context: Editor) {
        container.remove(original, false)
        container.add(current, true)
    }

    override fun undo(context: Editor) {
        container.remove(current, false)
        container.add(original, true)
    }
}

class TimeSigValueChange(val original: TimeSignature, var current: TimeSignature)
    : ReversibleAction<Editor> {

    private val container = original.container

    override fun redo(context: Editor) {
        container.remove(original)
        container.add(current)
    }

    override fun undo(context: Editor) {
        container.remove(current)
        container.add(original)
    }
}

class TimeSignatureAction(val timeSig: TimeSignature, val remove: Boolean) : ReversibleAction<Editor> {

    private fun add(container: TimeSignatures) {
        container.add(timeSig)
    }

    private fun remove(container: TimeSignatures) {
        container.remove(timeSig)
    }

    override fun redo(context: Editor) {
        if (remove) {
            remove(context.engine.timeSignatures)
        } else {
            add(context.engine.timeSignatures)
        }
        context.engine.recomputeCachedData()
    }

    override fun undo(context: Editor) {
        if (remove) {
            add(context.engine.timeSignatures)
        } else {
            remove(context.engine.timeSignatures)
        }
        context.engine.recomputeCachedData()
    }

}
