package io.github.chrislo27.bouncyroadmania.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher3
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.logging.Logger
import java.io.File

object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/chrislo27/RhythmHeavenRemixEditor/issues/273
        System.setProperty("jna.nosys", "true")

        BRMania.launchArguments = args.toList()

        val logger = Logger()
        val app = BRManiaApp(logger, File(System.getProperty("user.home") + "/.bouncyroadmania/logs/"))
        ToolboksDesktopLauncher3(app)
                .editConfig {
                    this.setAutoIconify(true)
                    this.setWindowedMode(app.emulatedSize.first, app.emulatedSize.second)
                    this.setWindowSizeLimits(640, 360, -1, -1)
                    this.setTitle(app.getTitle())
//                    this.fullscreen = false
                    val fpsArg = args.find { it.startsWith("--fps=") }
                    val idleFps = (if (fpsArg != null) {
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
                    this.setIdleFPS(idleFps)
                    this.setResizable(true)
                    this.useVsync(idleFps <= 60)
                    this.setInitialBackgroundColor(Color(0f, 0f, 0f, 1f))
                    this.setAudioConfig(250, 1024, 9)
                    this.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Logical)

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
