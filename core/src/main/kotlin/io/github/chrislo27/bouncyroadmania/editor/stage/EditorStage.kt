package io.github.chrislo27.bouncyroadmania.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.bouncyroadmania.editor.Editor
import io.github.chrislo27.bouncyroadmania.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.Stage


class EditorStage(val editor: Editor)
    : Stage<EditorScreen>(null, OrthographicCamera().apply { setToOrtho(false, 1280f, 720f) }, 1280f, 720f) {

}