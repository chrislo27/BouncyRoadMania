package io.github.chrislo27.bouncyroadmania.editor

import io.github.chrislo27.bouncyroadmania.engine.tracker.Tracker


sealed class ClickOccupation {

    interface TrackerBased {
        var finished: Boolean
        var final: Float
    }

    object None : ClickOccupation()

//    class Playback(val editor: Editor)
//        : ClickOccupation(), ReversibleAction<Engine>, TrackerBased {
//        val old = editor.remix.playbackStart
//        override var finished: Boolean = false
//        override var final: Float = Float.NaN
//            set(value) {
//                if (!java.lang.Float.isNaN(field)) {
//                    error("Attempt to set value to $value when already set to $field")
//                }
//                field = value
//            }
//
//        override fun redo(context: Engine) {
//            if (final == Float.NaN)
//                error("Final value was NaN which is impossible")
//            editor.remix.playbackStart = final
//        }
//
//        override fun undo(context: Engine) {
//            editor.remix.playbackStart = old
//        }
//    }
//
//    class Music(val editor: Editor, val middleClick: Boolean)
//        : ClickOccupation(), ReversibleAction<Engine>, TrackerBased {
//        val old = editor.remix.musicStartSec
//        override var finished: Boolean = false
//        override var final: Float = Float.NaN
//            set(value) {
//                if (!java.lang.Float.isNaN(field)) {
//                    error("Attempt to set value to $value when already set to $field")
//                }
//                field = value
//            }
//
//        override fun redo(context: Engine) {
//            if (final != final)
//                error("Final value was NaN which is impossible")
//            editor.remix.musicStartSec = final
//        }
//
//        override fun undo(context: Engine) {
//            editor.remix.musicStartSec = old
//        }
//    }

//    class CreatingSelection(val editor: Editor, val startPoint: Vector2)
//        : ClickOccupation() {
//        val oldSelection = editor.selection.toList()
//        val rectangle = Rectangle()
//
//        fun updateRectangle() {
//            val startX = startPoint.x
//            val startY = startPoint.y
//            val width = editor.renderer.trackCamera.getInputX() - startX
//            val height = editor.renderer.trackCamera.getInputY() - startY
//
//            if (width < 0) {
//                val abs = Math.abs(width)
//                rectangle.x = startX - abs
//                rectangle.width = abs
//            } else {
//                rectangle.x = startX
//                rectangle.width = width
//            }
//
//            if (height < 0) {
//                val abs = Math.abs(height)
//                rectangle.y = startY - abs
//                rectangle.height = abs
//            } else {
//                rectangle.y = startY
//                rectangle.height = height
//            }
//        }
//
//    }

//    class SelectionDrag(val editor: Editor,
//                        private val first: Entity,
//                        val clickedOn: Entity,
//                        val mouseOffset: Vector2,
//                        val isNew: Boolean,
//                        val isCopy: Boolean,
//                        val previousSelection: List<Entity>,
//                        val stretchType: StretchRegion)
//        : ClickOccupation() {
//
//        companion object {
//            fun copyBounds(selection: List<Entity>): Map<Entity, Rectangle> =
//                    selection.associateWith { Rectangle(it.bounds) }
//        }
//
//        val isNewOrCopy: Boolean = isNew || isCopy
//        val oldBounds: Map<Entity, Rectangle> = copyBounds(editor.selection)
//        val isStretching: Boolean by lazy { !isNewOrCopy && stretchType != StretchRegion.NONE }
//        private val selection: List<Entity>
//            get() = editor.selection
//        val isAllSpecial: Boolean by lazy {
//            selection.all { it is ModelEntity<*> && it.isSpecialEntity && it !is EndEngineEntity }
//        }
//        val isBottomSpecial: Boolean by lazy {
//            if (isAllSpecial) return@lazy true
//            val lowPoint = selection.minBy { it.bounds.y }?.bounds?.y ?: error("Nothing in selection")
//            val allBottom = selection.filter { MathUtils.isEqual(it.bounds.y, lowPoint) }
//
//            allBottom.all { it is ModelEntity<*> && it.isSpecialEntity && it !is EndEngineEntity }
//        }
//
//        val left: Float
//            get() = selection.minBy { it.bounds.x }?.bounds?.x ?: error("Nothing in selection")
//        val right: Float
//            get() {
//                val right = selection.maxBy { it.bounds.x + it.bounds.width } ?: error("Nothing in selection")
//                return right.bounds.x + right.bounds.width
//            }
//        val top: Float
//            get() {
//                val highest = selection.maxBy { it.bounds.y + it.bounds.height } ?: error("Nothing in selection")
//                return highest.bounds.y + highest.bounds.height
//            }
//        val bottom: Float
//            get() = selection.minBy { it.bounds.y }?.bounds?.y ?: error("Nothing in selection")
//
//        val width: Float by lazy {
//            right - left
//        }
//        val height: Int by lazy {
//            Math.round(top - bottom)
//        }
//
//        val lerpLeft: Float
//            get() = selection.minBy { it.bounds.x + it.lerpDifference.x }?.let { it.bounds.x + it.lerpDifference.x } ?: error("Nothing in selection")
//        val lerpRight: Float
//            get() {
//                val right = selection.maxBy { it.bounds.x + it.bounds.width + it.lerpDifference.x + it.lerpDifference.width } ?: error("Nothing in selection")
//                return right.bounds.x + right.bounds.width + right.lerpDifference.x + right.lerpDifference.width
//            }
//        val lerpTop: Float
//            get() {
//                val highest = selection.maxBy { it.bounds.y + it.bounds.height + it.lerpDifference.y + it.lerpDifference.height } ?: error("Nothing in selection")
//                return highest.bounds.y + highest.bounds.height + highest.lerpDifference.y + highest.lerpDifference.height
//            }
//        val lerpBottom: Float
//            get() = selection.minBy { it.bounds.y + it.lerpDifference.y }?.let { it.bounds.y + it.lerpDifference.y } ?: error("Nothing in selection")
//
//        private var firstSetPosition = true
//
//        fun setFirstPosition(x: Float, y: Float) {
//            // reducing object creation due to rapid calling
//            val oldFirstPosX = first.bounds.x
//            val oldFirstPosY = first.bounds.y
//            first.updateBounds {
//                first.bounds.setPosition(x, y)
//            }
//
//            selection.forEach { entity ->
//                if (entity === first)
//                    return@forEach
//                entity.updateBounds {
//                    entity.bounds.x = (entity.bounds.x - oldFirstPosX) + x
//                    entity.bounds.y = (entity.bounds.y - oldFirstPosY) + y
//                }
//            }
//        }
//
//        fun setPositionRelativeToMouse(snap: Float = editor.snap, intY: Boolean = true) {
//            if (firstSetPosition) {
//                firstSetPosition = false
//                editor.remix.entities.sortWith(Comparator { o1, o2 ->
//                    when {
//                        o1 in editor.selection && o2 !in editor.selection -> 1
//                        o1 !in editor.selection && o2 in editor.selection -> -1
//                        else -> 0
//                    }
//                })
//            }
//
//            val y = editor.camera.getInputY() - mouseOffset.y
//            setFirstPosition(MathHelper.snapToNearest(editor.camera.getInputX() - mouseOffset.x, snap),
//                    if (intY) Math.round(y).toFloat() else y)
//        }
//
//        fun isPlacementValid(): Boolean {
//            if (isInDeleteZone())
//                return false
//            if (top > editor.remix.trackCount)
//                return false
//
//            // EXCEPTIONS for the end entity
//            if (selection.any { it is EndEngineEntity } && editor.remix.entities.filter { it is EndEngineEntity }.size > 1)
//                return false
//
//            return editor.remix.entities.all {
//                it in selection || selection.all { sel ->
//                    !sel.bounds.intersects(it.bounds)
//                }
//            } && (!isStretching || selection.none { s -> selection.any { s != it && s.bounds.intersects(it.bounds) } })
//        }
//
//        fun isInDeleteZone(): Boolean {
//            return if (isBottomSpecial) {
//                bottom < -1.5f
//            } else {
//                bottom < -0.5f
//            }
//        }
//
//    }

    class TrackerResize(val tracker: Tracker<*>, val mouseOffset: Float, val left: Boolean)
        : ClickOccupation() {

        var beat: Float = tracker.beat
            private set
        var width: Float = tracker.width
            private set
        val text: String = tracker.text
        val renderLayer: Int
            get() = tracker.container.renderLayer

        fun normalizeWidth() {
            if (width < 0) {
                width = Math.abs(width)
                beat -= width
            }
        }

        fun isPlacementValid(): Boolean {
            return tracker.container.map.values.none {
                if (it === tracker) {
                    false
                } else {
                    (beat < it.beat + it.width && beat + width > it.beat) || it.beat == beat
                }
            }
        }

        fun updatePosition(newPos: Float) {
            val originalX = tracker.beat
            val originalEndX = tracker.endBeat

            if (left) {
                beat = newPos
                width = originalEndX - newPos
            } else {
                beat = originalX
                width = newPos - originalX
            }

            normalizeWidth()
        }

    }

}