package io.github.chrislo27.bouncyroadmania.registry

import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.*
import io.github.chrislo27.bouncyroadmania.engine.event.practice.SpawnTextBoxEvent


object EventRegistry {

    val map: Map<String, Instantiator>
    val list: List<Instantiator>

    init {
        val tempMap = mutableMapOf<String, Instantiator>()
        val tempList = mutableListOf<Instantiator>()

        fun add(instantiator: Instantiator) {
            tempMap[instantiator.id] = instantiator
            instantiator.deprecatedIDs.forEach { tempMap[it] = instantiator }
            tempList += instantiator
        }

        add(Instantiator("deploy", listOf(), "instantiator.deploy.name", true, "instantiator.deploy.summary", true, "instantiator.deploy.desc", true) { engine ->
            DeployEvent(engine, this)
        })
        add(Instantiator("skill_star", listOf(), "instantiator.skill_star.name", true, "instantiator.skill_star.summary", true, "instantiator.skill_star.desc", true) { engine ->
            SkillStarEvent(engine, this)
        })
        add(Instantiator("end", listOf(), "instantiator.end.name", true, "instantiator.end.summary", true, "instantiator.end.desc", true) { engine ->
            EndEvent(engine, this)
        })
        add(Instantiator("gradient_start", listOf(), "instantiator.gradient_start.name", true, "instantiator.gradient_start.summary", true, "instantiator.gradient_start.desc", true) { engine ->
            GradientChangeEvent(engine, this, true).apply {
                this.color.set(engine.gradientStart.initial)
                this.onColorChange()
            }
        })
        add(Instantiator("gradient_end", listOf(), "instantiator.gradient_end.name", true, "instantiator.gradient_end.summary", true, "instantiator.gradient_end.desc", true) { engine ->
            GradientChangeEvent(engine, this, false).apply {
                this.color.set(engine.gradientEnd.initial)
                this.onColorChange()
            }
        })
        add(Instantiator("bouncer_colour", listOf(), "instantiator.bouncer_colour.name", true, "instantiator.bouncer_colour.summary", true, "instantiator.bouncer_colour.desc", true) { engine ->
            BouncerColourEvent(engine, this).apply {
                this.color.set(this.tintTarget.initial)
                this.onColorChange()
            }
        })
        add(Instantiator("background_image", listOf(), "instantiator.background_image.name", true, "instantiator.background_image.summary", true, "instantiator.background_image.desc", true) { engine ->
            BgImageEvent(engine, this, false)
        })
        add(Instantiator("foreground_image", listOf(), "instantiator.foreground_image.name", true, "instantiator.foreground_image.summary", true, "instantiator.foreground_image.desc", true) { engine ->
            BgImageEvent(engine, this, true)
        })
        add(Instantiator("text_box", listOf(), "instantiator.text_box.name", true, "instantiator.text_box.summary", true, "instantiator.text_box.desc", true) { engine ->
            SpawnTextBoxEvent(engine, TextBox("", false), this)
        })
        add(Instantiator("spotlight", listOf(), "instantiator.spotlight.name", true, "instantiator.spotlight.summary", true, "instantiator.spotlight.desc", true) { engine ->
            SpotlightEvent(engine, this)
        })

        map = tempMap
        list = tempList
    }

}