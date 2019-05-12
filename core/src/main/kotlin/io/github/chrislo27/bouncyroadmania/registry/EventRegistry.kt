package io.github.chrislo27.bouncyroadmania.registry

import io.github.chrislo27.bouncyroadmania.engine.event.DeployEvent


object EventRegistry {

    val map: Map<String, Instantiator>
    val list: List<Instantiator>

    init {
        val tempMap = mutableMapOf<String, Instantiator>()
        val tempList = mutableListOf<Instantiator>()

        fun add(instantiator: Instantiator) {
            tempMap[instantiator.id] = instantiator
            tempList += instantiator
        }

        add(Instantiator("deploy", listOf(), "instantiator.deploy.name", true, "instantiator.deploy.summary", true, "instantiator.deploy.desc", true) { engine ->
            DeployEvent(engine, this)
        })
        add(Instantiator("end", listOf(), "instantiator.end.name", true, "instantiator.end.summary", true, "instantiator.end.desc", true) { engine ->
            DeployEvent(engine, this)
        })

        map = tempMap
        list = tempList
    }

}