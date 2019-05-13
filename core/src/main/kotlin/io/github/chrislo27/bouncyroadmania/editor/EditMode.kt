package io.github.chrislo27.bouncyroadmania.editor


enum class EditMode(val localizationKey: String) {

    ENGINE("editor.editMode.engine"), EVENTS("editor.editMode.events"), PARAMETERS("editor.editMode.params");

    companion object {
        val VALUES = values().toList()
    }

}