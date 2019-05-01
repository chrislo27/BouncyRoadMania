package io.github.chrislo27.bouncyroadmania.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.github.chrislo27.bouncyroadmania.BRManiaApp


fun BitmapFont.scaleFont(camera: OrthographicCamera) {
    this.setUseIntegerPositions(false)
    this.data.setScale(camera.viewportWidth / BRManiaApp.instance.defaultCamera.viewportWidth,
            camera.viewportHeight / BRManiaApp.instance.defaultCamera.viewportHeight)
}

fun BitmapFont.unscaleFont() {
    this.setUseIntegerPositions(true)
    this.data.setScale(1f)
}