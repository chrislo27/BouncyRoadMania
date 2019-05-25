package io.github.chrislo27.bouncyroadmania.engine.event


object EventPosComparator : Comparator<Event> {
    
    override fun compare(o1: Event?, o2: Event?): Int {
        if (o1 == null && o2 == null)
            return 0
        if (o2 == null)
            return 1
        if (o1 == null)
            return -1
        if (o1.bounds.x == o2.bounds.x) {
            return if (o1.bounds.y > o2.bounds.y) {
                -1
            } else if (o1.bounds.y < o2.bounds.y) {
                1
            } else 0
        }
        
        return if (o1.bounds.x > o2.bounds.x) {
            1
        } else {
            -1
        }
    }

}