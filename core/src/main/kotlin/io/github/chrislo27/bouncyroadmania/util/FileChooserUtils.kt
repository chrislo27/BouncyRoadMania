package io.github.chrislo27.bouncyroadmania.util

import io.github.chrislo27.bouncyroadmania.BRManiaApp
import java.io.File


private val userHomeFile: File = File(System.getProperty("user.home"))
private val desktopFile: File = userHomeFile.resolve("Desktop")

internal fun persistDirectory(prefName: String, file: File) {
    val main = BRManiaApp.instance
    main.preferences.putString(prefName, file.absolutePath)
    main.preferences.flush()
}

internal fun attemptRememberDirectory(prefName: String): File? {
    val main = BRManiaApp.instance
    val f: File = File(main.preferences.getString(prefName, null) ?: return null)

    if (f.exists() && f.isDirectory)
        return f

    return null
}

internal fun getDefaultDirectory(): File =
        if (!desktopFile.exists() || !desktopFile.isDirectory)
            userHomeFile
        else
            desktopFile