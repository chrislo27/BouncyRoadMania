package io.github.chrislo27.crossing.desktop

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.crossing.Crossing
import io.github.chrislo27.crossing.CrossingApp
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.logging.Logger
import java.io.File

object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/chrislo27/RhythmHeavenRemixEditor/issues/273
        System.setProperty("jna.nosys", "true")

        Crossing.launchArguments = args.toList()

        val logger = Logger()
        val app = CrossingApp(logger, File(System.getProperty("user.home") + "/.crossing/logs/"))
        ToolboksDesktopLauncher(app)
                .editConfig {
                    this.width = app.emulatedSize.first
                    this.height = app.emulatedSize.second
                    this.title = app.getTitle()
                    this.fullscreen = false
                    val fpsArg = args.find { it.startsWith("--fps=") }
                    this.foregroundFPS = (if (fpsArg != null) {
                        val num = fpsArg.substringAfter('=')
                        val parsed = num.toIntOrNull()
                        val adjusted = parsed?.coerceAtLeast(30) ?: 60
                        if (parsed == null) {
                            logger.info("Failed to parse manual FPS: $num")
                        } else {
                            logger.info("Manually setting FPS to $adjusted (requested: $num)")
                        }
                        adjusted
                    } else 60).coerceAtLeast(30)
                    this.backgroundFPS = this.foregroundFPS.coerceIn(30, 60)
                    this.resizable = true
                    this.vSyncEnabled = this.foregroundFPS <= 60
                    this.initialBackgroundColor = Color(0f, 0f, 0f, 1f)
                    this.allowSoftwareMode = true
                    this.audioDeviceSimultaneousSources = 250
                    this.useHDPI = true

                    LazySound.loadLazilyWithAssetManager = "--force-lazy-sound-load" !in args

                    val sizes: List<Int> = listOf(256, 128, 64, 32, 24, 16)
//                    sizes.forEach {
//                        this.addIcon("images/icon/$it.png", Files.FileType.Internal)
//                    }
//
//                    listOf(24, 16).forEach {
//                        this.addIcon("images/icon/$it.png", Files.FileType.Internal)
//                    }
                }
                .launch()
    }

}
