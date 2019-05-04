package io.github.chrislo27.bouncyroadmania.util

import com.badlogic.gdx.graphics.Color
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.tinyfd.TinyFileDialogs.*
import java.io.File
import java.nio.ByteBuffer


object TinyFDWrapper {

    data class Filter(val extensions: List<String>, val description: String)

    fun openFile(title: String, defaultFile: String?, multipleSelection: Boolean, filter: Filter?): File? {
        return if (filter == null) {
            val path = tinyfd_openFileDialog(title, defaultFile, null, null, multipleSelection) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(stack.UTF8(it))
                }
                filterPatterns.flip()

                val path = tinyfd_openFileDialog(title, defaultFile, filterPatterns, filter.description, multipleSelection)
                        ?: return null
                File(path)
            }
        }
    }

    fun saveFile(title: String, defaultFile: String?, filter: Filter?): File? {
        return if (filter == null) {
            val path = tinyfd_saveFileDialog(title, defaultFile, null, null) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(stack.UTF8(it))
                }
                filterPatterns.flip()

                val path = tinyfd_saveFileDialog(title, defaultFile, filterPatterns, filter.description) ?: return null
                File(path)
            }
        }
    }

    fun selectFolder(title: String, defaultFolder: String): File? {
        val path = tinyfd_selectFolderDialog(title, defaultFolder) ?: return null
        return File(path)
    }

    fun selectColor(title: String, defaultHexColor: String): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val hex: String? = tinyfd_colorChooser(title, defaultHexColor, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

    fun selectColor(title: String, defaultColor: Color?): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val def = if (defaultColor == null) "#FFFFFF" else "#${defaultColor.toString().take(6)}"
            val hex: String? = tinyfd_colorChooser(title, def, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

}
