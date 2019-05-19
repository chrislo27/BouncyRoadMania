package io.github.chrislo27.bouncyroadmania

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.version.Version


object BRMania {

    const val TITLE = "Bouncy Road Mania"
    val VERSION: Version = Version(0, 1, 0, "")
    const val WIDTH = 1280
    const val HEIGHT = 720
    val BRMANIA_FOLDER: FileHandle by lazy { (Gdx.files.external(".bouncyroadmania/")).apply(FileHandle::mkdirs) }
    val tmpMusic: FileHandle by lazy {
        BRMANIA_FOLDER.child("tmpMusic/").apply {
            mkdirs()
        }
    }

    val FILE_EXTENSION: String = "brmania"
    const val RELEASE_API_URL = "https://api.github.com/repos/chrislo27/BouncyRoadMania/releases/latest"

    lateinit var launchArguments: List<String>

}