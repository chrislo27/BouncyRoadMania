package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.engine.entity.*
import io.github.chrislo27.bouncyroadmania.engine.event.EndEvent
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.engine.event.PlaybackCompletion
import io.github.chrislo27.bouncyroadmania.engine.input.InputResult
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.engine.timesignature.TimeSignatures
import io.github.chrislo27.bouncyroadmania.engine.tracker.TrackerContainer
import io.github.chrislo27.bouncyroadmania.engine.tracker.musicvolume.MusicVolumes
import io.github.chrislo27.bouncyroadmania.renderer.PaperProjection
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSound
import io.github.chrislo27.bouncyroadmania.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import io.github.chrislo27.toolboks.version.Version
import java.util.*
import kotlin.properties.Delegates


class Engine : Clock(), Disposable {

    companion object {
        private val TMP_MATRIX = Matrix4()

        val MAX_OFFSET_SEC: Float = 5f / 60
        val ACE_OFFSET: Float = 1f / 60
        val GOOD_OFFSET: Float = 3f / 60
        val BARELY_OFFSET: Float = 4f / 60

        val MIN_TRACK_COUNT: Int = 4
        val MAX_TRACK_COUNT: Int = 4
        val DEFAULT_TRACK_COUNT: Int = MIN_TRACK_COUNT
    }

    var version: Version = BRMania.VERSION.copy()
    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val projector = PaperProjection(2f)

    val timeSignatures: TimeSignatures = TimeSignatures()
    val musicVolumes: MusicVolumes = MusicVolumes()
    val trackers: List<TrackerContainer<*>> = listOf(tempos, musicVolumes)
    val trackersReverseView: List<TrackerContainer<*>> = trackers.asReversed()
    override var playState: PlayState by Delegates.vetoable(super.playState) { _, old, value ->
        if (old != value) {
            if (value == PlayState.PLAYING && tempos.secondsMap.isEmpty()) {
//                return@vetoable false
            }
            val music = music
            entities.forEach { it.onPlayStateChanged(old, value) }
            lastBounceTinkSound.clear()

            when (value) {
                PlayState.STOPPED -> {
                    AssetRegistry.stopAllSounds()
                    music?.music?.pause()
                    BeadsSoundSystem.stop()
                    entities.clear()
                    addBouncers()
                }
                PlayState.PAUSED -> {
                    AssetRegistry.pauseAllSounds()
                    music?.music?.pause()
                    BeadsSoundSystem.pause()
                }
                PlayState.PLAYING -> {
                    lastMusicPosition = -1f
                    scheduleMusicPlaying = true
                    AssetRegistry.resumeAllSounds()
                    if (old == PlayState.STOPPED) {
                        recomputeCachedData()
                        inputResults.clear()
                        seconds = tempos.beatsToSeconds(playbackStart)
                        events.forEach {
                            if (it.getUpperUpdateableBound() < beat) {
                                if (it.shouldAlwaysBeSimulated) {
                                    it.playbackCompletion = PlaybackCompletion.PLAYING
                                    it.onStart()
                                    it.whilePlaying()
                                    it.playbackCompletion = PlaybackCompletion.FINISHED
                                    it.onEnd()
                                }
                                it.playbackCompletion = PlaybackCompletion.FINISHED
                            } else {
                                it.playbackCompletion = PlaybackCompletion.WAITING
                            }
                        }
                        gradientCurrentStart.set(gradientStart)
                        gradientCurrentEnd.set(gradientEnd)

                        lastMetronomeMeasure = Math.ceil(playbackStart - 1.0).toInt()
                        lastMetronomeMeasurePart = -1
                    }
                    BeadsSoundSystem.resume()
                    if (music != null) {
                        if (seconds >= musicStartSec) {
                            music.music.play()
                            setMusicVolume()
                            seekMusic()
                        } else {
                            music.music.stop()
                        }
                    }
                }
            }

            listeners.keys.forEach { it.onPlayStateChanged(old, value) }
        }
        true
    }

    var trackCount: Int = DEFAULT_TRACK_COUNT
        set(value) {
            field = value
            events.filterIsInstance<EndEvent>().forEach {
                it.bounds.y = 0f
                it.bounds.height = value.toFloat()
            }
        }
    var duration: Float = Float.POSITIVE_INFINITY
        private set
    var lastPoint: Float = 0f
        private set

    var playbackStart: Float = 0f
    var musicStartSec: Float = 0f
    var metronome: Boolean = false
    private var lastMetronomeMeasure: Int = -1
    private var lastMetronomeMeasurePart: Int = -1
    var music: MusicData? = null
        set(value) {
            field?.dispose()
            field = value
        }
    var isMusicMuted: Boolean = false
    private var lastMusicPosition: Float = -1f
    private var scheduleMusicPlaying = true
    @Volatile
    var musicSeeking = false
    var loopIndex: Int = 0
        private set
    
    val events: List<Event> = mutableListOf()
    val inputResults: MutableList<InputResult> = mutableListOf()
    var expectedNumInputs: Int = 0

    val entities: MutableList<Entity> = mutableListOf()
    var bouncers: List<Bouncer> = listOf()
    lateinit var yellowBouncer: YellowBouncer
        private set
    lateinit var redBouncer: RedBouncer
        private set
    val lastBounceTinkSound: MutableMap<String, Float> = mutableMapOf()

    var requiresPlayerInput: Boolean = true
    val listeners: WeakHashMap<EngineEventListener, Unit> = WeakHashMap()

    // Visuals
    val gradientEnd: Color = Color(1f, 1f, 1f, 1f).set(Color.valueOf("0296FFFF"))
    val gradientStart: Color = Color(0f, 0f, 0f, 1f)
    val gradientCurrentEnd: Color = Color(1f, 1f, 1f, 1f).set(gradientEnd)
    val gradientCurrentStart: Color = Color(0f, 0f, 0f, 1f).set(gradientStart)
    var gradientDirection: GradientDirection = GradientDirection.VERTICAL

    fun addBouncers() {
        entities.removeAll(bouncers)
        bouncers = listOf()

        val radius = 1200f
        val bouncers = mutableListOf<Bouncer>()
        // midpoint index = 8
        for (i in -1..15) {
            val angle = (180.0 * (i / 14.0)) - 90
            val bouncer: Bouncer = if (i == 13) {
                RedBouncer(this).apply {
                    redBouncer = this
                }
            } else if (i == 12) {
                YellowBouncer(this).apply {
                    yellowBouncer = this
                }
            } else {
                Bouncer(this)
            }

            if (i !in 0 until 15) {
                bouncer.isSilent = true
            } else if (i == 14) {
                bouncer.soundHandle = "sfx_cymbal"
            }

            val sin = Math.sin(Math.toRadians(angle)).toFloat()
            val z: Float = (-sin + 1) * 0.3f

            bouncer.posY = Interpolation.sineIn.apply(640f, 150f, i / 14f)

            bouncer.posX = radius * if (i <= 8) Interpolation.sineOut.apply(i / 8f) else Interpolation.sineOut.apply(1f - ((i - 8) / 6f))
            bouncer.posZ = z

            bouncers += bouncer
        }
        this.bouncers = bouncers
        entities.addAll(bouncers)
    }

    init {
        addBouncers()
    }

    fun addEvent(event: Event) {
        if (event !in events) {
            (events as MutableList) += event
            recomputeCachedData()
            listeners.keys.forEach { it.onEventAdded(event) }
        }
    }

    fun removeEvent(event: Event) {
        val oldSize = events.size
        (events as MutableList) -= event
        if (events.size != oldSize) {
            recomputeCachedData()
            listeners.keys.forEach { it.onEventRemoved(event) }
        }
    }

    fun addAllEvents(events: Collection<Event>) {
        this.events as MutableList
        val oldSize = this.events.size
        events.forEach {
            if (it !in this.events) {
                this.events += it
                listeners.keys.forEach { l -> l.onEventAdded(it) }
            }
        }
        if (this.events.size != oldSize) {
            recomputeCachedData()
        }
    }

    fun removeAllEvents(events: Collection<Event>) {
        this.events as MutableList
        val oldSize = this.events.size
        this.events.removeAll(events)
        events.forEach { listeners.keys.forEach { l -> l.onEventRemoved(it) } }
        if (this.events.size != oldSize) {
            recomputeCachedData()
        }
    }
    
    fun computeScore(): Float {
        return (inputResults.sumByDouble { it.inputScore.weight.toDouble() } / expectedNumInputs.coerceAtLeast(1) * 100).toFloat()
    }

    private fun setMusicVolume() {
        val music = music ?: return
        val shouldBe = if (isMusicMuted) 0f else musicVolumes.volumeAt(beat)
        if (music.music.getVolume() != shouldBe) {
            music.music.setVolume(shouldBe)
        }
    }

    private fun seekMusic() {
        val music = music ?: return
        musicSeeking = true
        val loops = music.music.isLooping()
        val s = seconds
        if (loops) {
            val loopPos = (s - musicStartSec) % music.music.getDuration()
            music.music.setPosition(loopPos)
            loopIndex = ((seconds - musicStartSec) / music.music.getDuration()).toInt()
        } else {
            music.music.setPosition(s - musicStartSec)
        }
        musicSeeking = false
    }

    fun recomputeCachedData() {
        lastPoint = events.firstOrNull { it is EndEvent }?.bounds?.x ?: events.maxBy { it.bounds.maxX }?.bounds?.maxX ?: 0f
        duration = events.firstOrNull { it is EndEvent }?.bounds?.x ?: Float.POSITIVE_INFINITY
    }

    fun eventUpdate(event: Event) {
        if (event.playbackCompletion == PlaybackCompletion.WAITING) {
            if (event.isUpdateable(beat)) {
                event.playbackCompletion = PlaybackCompletion.PLAYING
                event.onStart()
            }
        }

        if (event.playbackCompletion == PlaybackCompletion.PLAYING) {
            event.whilePlaying()
            if (beat >= event.getUpperUpdateableBound()) {
                event.playbackCompletion = PlaybackCompletion.FINISHED
                event.onEnd()
            }
        }
    }

    override fun update(delta: Float) {
        // No super update
        if (playState != PlayState.PLAYING)
            return

        val music: MusicData? = music
        music?.music?.update(if (playState == PlayState.PLAYING) (delta * 0.75f) else delta)

        // Timing
        seconds += delta

        if (music != null) {
            if (scheduleMusicPlaying && seconds >= musicStartSec) {
                music.music.play()
                scheduleMusicPlaying = false
            }
            if (music.music.isPlaying()) {
                val oldPosition = lastMusicPosition
                val newPosition = music.music.getPosition()
                lastMusicPosition = newPosition

                if (oldPosition != newPosition) {
                    seconds = if (music.music.isLooping()) {
                        if (newPosition < oldPosition) {
                            loopIndex++
                        }
                        newPosition + musicStartSec + loopIndex * music.music.getDuration()
                    } else {
                        newPosition + musicStartSec
                    }
                }

                setMusicVolume()
            }
        }

        events.forEach { event ->
            if (event.playbackCompletion != PlaybackCompletion.FINISHED) {
                eventUpdate(event)
            }
        }

        // Updating
        entities.forEach {
            it.renderUpdate(delta)
        }
        entities.removeIf { it.kill }

        val measure = timeSignatures.getMeasure(beat).takeIf { it >= 0 } ?: Math.floor(beat.toDouble()).toInt()
        val measurePart = timeSignatures.getMeasurePart(beat)
        if (lastMetronomeMeasure != measure || lastMetronomeMeasurePart != measurePart) {
            lastMetronomeMeasure = measure
            lastMetronomeMeasurePart = measurePart
            if (metronome) {
                val isStartOfMeasure = measurePart == 0
                AssetRegistry.get<BeadsSound>("sfx_cowbell").play(loop = false, volume = 1.25f, pitch = if (isStartOfMeasure) 1.5f else 1.1f)
            }
        }

        if (playState != PlayState.STOPPED && beat >= duration) {
            playState = PlayState.STOPPED
        }
    }

    fun render(batch: SpriteBatch) {
        camera.update()
        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = camera.combined
        batch.begin()
        // gradient
        if (gradientDirection == GradientDirection.VERTICAL) {
            batch.drawQuad(0f, 0f, gradientCurrentStart, camera.viewportWidth, 0f, gradientCurrentStart, camera.viewportWidth, camera.viewportHeight, gradientCurrentEnd, 0f, camera.viewportHeight, gradientCurrentEnd)
        } else {
            batch.drawQuad(0f, 0f, gradientCurrentStart, camera.viewportWidth, 0f, gradientCurrentEnd, camera.viewportWidth, camera.viewportHeight, gradientCurrentEnd, 0f, camera.viewportHeight, gradientCurrentStart)
        }
        projector.render(batch, entities)
        batch.end()
        batch.projectionMatrix = TMP_MATRIX
    }

    fun getBouncerForInput(inputType: InputType): Bouncer {
        return when (inputType) {
            InputType.A -> yellowBouncer
            InputType.DPAD -> redBouncer
        }
    }

    fun fireInput(inputType: InputType) {
        val bouncer = getBouncerForInput(inputType)
        val any = entities.filterIsInstance<Ball>().fold(false) { acc, it ->
            it.onInput(inputType) || acc
        }
        bouncer.bounceAnimation()
        if (!any) {
            // play dud sound
            AssetRegistry.get<BeadsSound>("sfx_dud_${if (inputType == InputType.A) "right" else "left"}").play(volume = 0.75f)
        }
    }

    override fun dispose() {
        listeners.clear()
        music?.dispose()
        BeadsSoundSystem.stop() // Ensures that the GC can collect the music data
    }

}