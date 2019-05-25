package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.GdxRuntimeException
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.engine.event.InstantiatedEvent
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignature
import io.github.chrislo27.bouncyroadmania.registry.EventRegistry
import io.github.chrislo27.bouncyroadmania.util.JsonHandler
import io.github.chrislo27.bouncyroadmania.util.fromJsonString
import io.github.chrislo27.bouncyroadmania.util.toJsonString
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.version.Version
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


fun Engine.toEngineJson(isAutosave: Boolean): ObjectNode {
    val root = JsonHandler.OBJECT_MAPPER.createObjectNode()

    root.put("version", BRMania.VERSION.toString())
    root.put("playbackStart", playbackStart)
    root.put("musicStartSec", musicStartSec)
    root.put("trackCount", trackCount)
    root.put("isAutosave", isAutosave)

    root.put("gradientFirst", gradientStart.initial.toJsonString())
    root.put("gradientLast", gradientEnd.initial.toJsonString())
    root.put("gradientDirection", gradientDirection.name)

    // bouncers
    run {
        val obj = root.putObject("bouncers")
        val tint = obj.putObject("tint")
        tint.put("normal", normalBouncerTint.initial.toJsonString())
        tint.put("a", aBouncerTint.initial.toJsonString())
        tint.put("dpad", dpadBouncerTint.initial.toJsonString())
    }
    
    // results text
    run {
        val obj = root.putObject("resultsText")
        val text = resultsText
        obj.put("title", text.title)
        obj.put("ok", text.ok)
        obj.put("firstPositive", text.firstPositive)
        obj.put("firstNegative", text.firstNegative)
        obj.put("secondPositive", text.secondPositive)
        obj.put("secondNegative", text.secondNegative)
    }

    // textures
    root.putArray("textures").also { ar ->
        textures.forEach { ar.add(it.key) }
    }

    // music
    run {
        val obj = root.putObject("musicData")
        val music = music

        obj.put("present", music != null)

        if (music != null) {
            obj.put("filename", music.handle.name())
            obj.put("extension", music.handle.extension())
        }
    }

    // events
    val eventsNode = root.putArray("events")
    events.filterIsInstance<InstantiatedEvent>().forEach { event ->
        event.toJson(eventsNode.addObject())
    }

    // trackers
    run {
        val trackers = root.putObject("trackers")

        trackers.set("tempos", tempos.toTree(trackers.objectNode()))
        trackers.set("musicVolumes", musicVolumes.toTree(trackers.objectNode()))
    }

    // time signatures
    run {
        val timeSigs = root.putArray("timeSignatures")

        timeSignatures.map.values.forEach {
            val node = timeSigs.addObject()
            node.put("beat", it.beat)
            node.put("divisions", it.beatsPerMeasure)
            node.put("beatUnit", it.beatUnit)
            node.put("measure", it.measure)
        }
    }

    return root
}

fun Engine.fromEngineJson(root: ObjectNode) {
    version = Version.fromString(root["version"].textValue())
    playbackStart = root["playbackStart"].floatValue()
    musicStartSec = root["musicStartSec"].floatValue()
    trackCount = root["trackCount"].intValue()

    gradientStart.initial.fromJsonString(root["gradientFirst"]?.asText())
    gradientEnd.initial.fromJsonString(root["gradientLast"]?.asText())
    gradientStart.reset()
    gradientEnd.reset()
    gradientDirection = GradientDirection.VALUES.find { it.name == root["gradientDirection"]?.asText() } ?: gradientDirection

    // bouncers
    val bouncersObj = root["bouncers"] as? ObjectNode
    if (bouncersObj != null) {
        // tints
        val tintObj = bouncersObj["tint"] as? ObjectNode
        if (tintObj != null) {
            normalBouncerTint.initial.fromJsonString(tintObj["normal"]?.asText())
            aBouncerTint.initial.fromJsonString(tintObj["a"]?.asText())
            dpadBouncerTint.initial.fromJsonString(tintObj["dpad"]?.asText())
            normalBouncerTint.reset()
            aBouncerTint.reset()
            dpadBouncerTint.reset()
        }
    }
    
    val resultsTextObj = root["resultsText"] as? ObjectNode
    if (resultsTextObj != null) {
        val stock = ResultsText()
        this.resultsText = ResultsText(resultsTextObj["title"]?.asText() ?: stock.title, resultsTextObj["ok"]?.asText() ?: stock.ok, resultsTextObj["firstPositive"]?.asText() ?: stock.firstPositive, resultsTextObj["firstNegative"]?.asText() ?: stock.firstNegative, resultsTextObj["secondPositive"]?.asText() ?: stock.secondPositive, resultsTextObj["secondNegative"]?.asText() ?: stock.secondNegative)
    }

    removeAllEvents(events.toList())
    entities.clear()
    addBouncers()

    // events
    val eventsArray = root["events"] as ArrayNode
    eventsArray.filterIsInstance<ObjectNode>()
            .filter { it.has("i") }
            .forEach { node ->
                val id = node["i"].textValue()
                val instantiator = EventRegistry.map[id]
                if (instantiator != null) {
                    val evt = instantiator.factory(instantiator, this)
                    evt.fromJson(node)
                    addEvent(evt)
                } else {
                    Toolboks.LOGGER.warn("Missing instantiator \"$id\"")
                }
            }

    // trackers
    run {
        val trackers = root.get("trackers") as ObjectNode

        tempos.fromTree(trackers["tempos"] as ObjectNode)
        musicVolumes.fromTree(trackers["musicVolumes"] as ObjectNode)
    }

    // time signatures
    run {
        val timeSigs = root.get("timeSignatures") as ArrayNode
        timeSigs.filterIsInstance<ObjectNode>().forEach {
            timeSignatures.add(TimeSignature(timeSignatures, it["beat"].asDouble().toFloat(), it["divisions"].asInt(4), it["beatUnit"]?.asInt(4) ?: 4))
        }
    }

    this.recomputeCachedData()
}

fun Engine.pack(stream: ZipOutputStream, isAutosave: Boolean) {
    val node = this.toEngineJson(isAutosave)
    stream.setComment("Bouncy Road Mania save file - ${BRMania.VERSION}")
    stream.putNextEntry(ZipEntry("data.json"))
    JsonHandler.toJson(node, stream)
    stream.closeEntry()

    val musicNode = node["musicData"] as ObjectNode
    if (musicNode["present"].booleanValue()) {
        stream.putNextEntry(ZipEntry("music.bin"))
        val buf = this.music!!.handle.read(2048)
        buf.copyTo(stream)
        buf.close()
        stream.closeEntry()
    }

    fun writeTexture(stream: OutputStream, tex: Texture) {
        if (!tex.textureData.isPrepared) {
            tex.textureData.prepare()
        }
        val pixmap: Pixmap = tex.textureData.consumePixmap()

        try {
            val writer = PixmapIO.PNG((pixmap.width.toFloat() * pixmap.height.toFloat() * 1.5f).toInt()) // Guess at deflated size.
            try {
                writer.setFlipY(false)
                writer.write(stream, pixmap)
            } finally {
                writer.dispose()
            }
        } catch (ex: IOException) {
            throw GdxRuntimeException("Error writing PNG", ex)
        } finally {
            if (tex.textureData.disposePixmap()) {
                pixmap.dispose()
            }
        }
    }
    
    textures.forEach { (key, tex) -> 
        stream.putNextEntry(ZipEntry("textures/$key.png"))
        writeTexture(stream, tex)
        stream.closeEntry()
    }
}

fun Engine.saveTo(file: File, isAutosave: Boolean) {
    if (!file.exists()) {
        file.createNewFile()
    }
    val stream = ZipOutputStream(FileOutputStream(file))
    this.pack(stream, isAutosave)
    stream.close()
}

fun Engine.unpack(zip: ZipFile, progressListener: (Float) -> Unit = {}) {
    val jsonStream = zip.getInputStream(zip.getEntry("data.json"))
    val objectNode: ObjectNode = JsonHandler.OBJECT_MAPPER.readTree(jsonStream) as ObjectNode
    jsonStream.close()

    val musicNode = objectNode["musicData"] as ObjectNode
    val musicPresent = musicNode["present"].booleanValue()

    val result = this.fromEngineJson(objectNode)

    if (musicPresent) {
        val folder = BRMania.tmpMusic
        val fh = folder.child(musicNode["filename"].asText(null) ?: error("Could not find music filename"))
        val musicStream = zip.getInputStream(zip.getEntry("music.bin"))
        fh.write(musicStream, false)
        musicStream.close()

        this.music = MusicData(fh, this, progressListener)
    }
    
    val texs = objectNode["textures"] as? ArrayNode?
    if (texs != null) {
        textures as MutableMap
        texs.forEach { node ->
            val name = node.asText()
            textures[name] = AssetRegistry.missingTexture
            val entry = zip.getEntry("textures/$name.png")
            val bytes = zip.getInputStream(entry).let { stream ->
                val b = stream.readBytes()
                stream.close()
                b
            }
            Gdx.app.postRunnable {
                try {
                    val texture = Texture(Pixmap(bytes, 0, bytes.size))
                    textures[name] = texture
                } catch (t: Throwable) {
                    Toolboks.LOGGER.warn("Failed to load texture with key $name")
                    t.printStackTrace()
                }
            }
        }
    }

    this.recomputeCachedData()

    return result
}
