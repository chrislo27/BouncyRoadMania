package io.github.chrislo27.bouncyroadmania.engine

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
import io.github.chrislo27.toolboks.version.Version
import java.io.File
import java.io.FileOutputStream
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
    root.put("gradientFirst", gradientStart.toJsonString())
    root.put("gradientLast", gradientEnd.toJsonString())
    root.put("gradientDirection", gradientDirection.name)

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
    
    gradientStart.fromJsonString(root["gradientFirst"]?.asText())
    gradientEnd.fromJsonString(root["gradientLast"]?.asText())
    gradientDirection = GradientDirection.VALUES.find { it.name == root["gradientDirection"]?.asText() } ?: gradientDirection

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
}

fun Engine.saveTo(file: File, isAutosave: Boolean) {
    if (!file.exists()) {
        file.createNewFile()
    }
    val stream = ZipOutputStream(FileOutputStream(file))
    this.pack(stream, isAutosave)
    stream.close()
}

fun Engine.unpack(zip: ZipFile) {
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

        this.music = MusicData(fh, this)
    }

    return result
}
