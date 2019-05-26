package io.github.chrislo27.bouncyroadmania.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.bouncyroadmania.BRManiaApp
import io.github.chrislo27.bouncyroadmania.PreferenceKeys
import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.MusicData
import io.github.chrislo27.bouncyroadmania.engine.PlayState
import io.github.chrislo27.bouncyroadmania.engine.TextBox
import io.github.chrislo27.bouncyroadmania.engine.event.DeployEvent
import io.github.chrislo27.bouncyroadmania.engine.event.Event
import io.github.chrislo27.bouncyroadmania.engine.event.SimpleEvent
import io.github.chrislo27.bouncyroadmania.engine.event.practice.PracticeGroupEvent
import io.github.chrislo27.bouncyroadmania.engine.event.practice.SpawnTextBoxEvent
import io.github.chrislo27.bouncyroadmania.engine.event.practice.XMoreTimesEvent
import io.github.chrislo27.bouncyroadmania.engine.tracker.musicvolume.MusicVolumeChange
import io.github.chrislo27.bouncyroadmania.engine.tracker.tempo.TempoChange
import io.github.chrislo27.bouncyroadmania.registry.EventRegistry
import io.github.chrislo27.bouncyroadmania.util.Swing
import io.github.chrislo27.bouncyroadmania.util.TempoUtils
import io.github.chrislo27.bouncyroadmania.util.transition.FadeOut
import io.github.chrislo27.bouncyroadmania.util.transition.WipeFrom
import io.github.chrislo27.bouncyroadmania.util.transition.WipeTo
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class PracticeScreen(main: BRManiaApp, engine: Engine) : PlayingScreen(main, engine) {

    init {
        restartButton.enabled = false
        robotModeButton.visible = false
    }

    override fun onQuit() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    override fun onEnd() {
        main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
    }

}

enum class PracticeStage {
    STANDARD, LONG_SHORT_FAST, SKILL_STAR
}

/**
 * A black screen to hide loading of the practice music
 */
class LoadingPracticeScreen(main: BRManiaApp, val practiceStage: PracticeStage) : ToolboksScreen<BRManiaApp, LoadingPracticeScreen>(main) {

    private fun createStandardPractice(): Engine {
        val engine = Engine()
        engine.music = MusicData(Gdx.files.internal("music/practice.ogg"), engine).apply {
            this.music.setLooping(true)
        }

        /*
        Order of events:
        Don't let the bouncing\nballs fall!
        Press [A] then [+] to bounce\nthe ball!
        [practice 2x with one-beat]
        Get ready!
        What, what? TWO balls\nincoming?!
        [practice 2x with two one-beat balls separated by two beats]
        Way to bounce!
        Uh-oh! The next ball is\ncoming in fast!
        [practice 2x with half-beat ball]
        Amazing!
        You've got the skills.\nReady for the real thing?
         */

        val textBoxInst = EventRegistry.map.getValue("text_box")
        val deployInst = EventRegistry.map.getValue("deploy")

        // Populate
        with(engine) {
            playbackStart = 0f
            musicStartSec = 1.5f
            tempos.add(TempoChange(tempos, 0f, 60f, Swing.STRAIGHT, 0f))
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.standard.stage1-1"], true), textBoxInst).apply {
                this.bounds.x = 0f
                this.bounds.width = 0.5f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.standard.stage1-2"], true), textBoxInst).apply {
                this.bounds.x = 0.5f
                this.bounds.width = 0.5f
            })
            tempos.add(TempoChange(tempos, 1.5f, 154f, Swing.STRAIGHT, 0f))
            addEvent(XMoreTimesEvent(engine, 2).apply {
                this.bounds.x = 1.5f
            })
            val firstPractice: PracticeGroupEvent.(Float) -> List<Event> = { offset ->
                listOf(
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 1f
                            this.bounds.x = offset - this.bounds.width
                        }
                )
            }
            val secondPractice: PracticeGroupEvent.(Float) -> List<Event> = { offset ->
                listOf(
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 1f
                            this.bounds.x = offset - this.bounds.width
                        },
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 1f
                            this.bounds.x = offset - this.bounds.width + 2f
                        })
            }
            val thirdPractice: PracticeGroupEvent.(Float) -> List<Event> = { offset ->
                listOf(
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 0.5f
                            this.bounds.x = offset - this.bounds.width
                        }
                )
            }
            val thirdPracticeEnd: PracticeGroupEvent.(Engine) -> Unit = {
                // Add music fade
                val origin = bounds.maxX + 2f
                musicVolumes.add(MusicVolumeChange(musicVolumes, bounds.maxX, 2f, 0))
                musicVolumes.add(MusicVolumeChange(musicVolumes, origin + 4f, 0f, 100))
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage4-1"], true), textBoxInst).apply {
                    this.bounds.x = origin + 1f
                    this.bounds.width = 0.5f
                })
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage4-2"], true), textBoxInst).apply {
                    this.bounds.x = origin + 2f
                    this.bounds.width = 0.5f
                })
                addEvent(SimpleEvent(engine, {
                    engine.playState = PlayState.STOPPED
                    main.preferences.putBoolean(PreferenceKeys.PRACTICE_COMPLETE_PREFIX + PracticeStage.STANDARD.name, true).flush()
                }).apply {
                    this.bounds.x = origin + 4f
                })
            }
            val secondPracticeEnd: PracticeGroupEvent.(Engine) -> Unit = {
                // Add music fade
                val origin = bounds.maxX + 2f
                musicVolumes.add(MusicVolumeChange(musicVolumes, bounds.maxX, 2f, 0))
                musicVolumes.add(MusicVolumeChange(musicVolumes, origin + 6f, 0f, 100))
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage3-1"], true), textBoxInst).apply {
                    this.bounds.x = origin + 1f
                    this.bounds.width = 0.5f
                })
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage3-2"], true), textBoxInst).apply {
                    this.bounds.x = origin + 2f
                    this.bounds.width = 0.5f
                })
                addEvent(XMoreTimesEvent(engine, 2).apply {
                    this.bounds.x = origin + 4f
                })
                addEvent(SimpleEvent(engine, {
                    resetMusic()
                    musicStartSec = tempos.beatsToSeconds(origin + 6f)
                    seekMusic()
                }).apply {
                    this.bounds.x = origin + 6f
                })
                // Practice group 3
                addEvent(PracticeGroupEvent(engine, thirdPractice, thirdPracticeEnd, 8f - 7f).apply {
                    this.bounds.x = origin + 6f
                    this.bounds.width = 7f
                })
            }
            val firstPracticeEnd: PracticeGroupEvent.(Engine) -> Unit = {
                // Add music fade
                val origin = bounds.maxX + 2f
                musicVolumes.add(MusicVolumeChange(musicVolumes, bounds.maxX, 2f, 0))
                musicVolumes.add(MusicVolumeChange(musicVolumes, origin + 6f, 0f, 100))
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage2-1"], true), textBoxInst).apply {
                    this.bounds.x = origin + 1f
                    this.bounds.width = 0.5f
                })
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.standard.stage2-2"], true), textBoxInst).apply {
                    this.bounds.x = origin + 2f
                    this.bounds.width = 0.5f
                })
                addEvent(XMoreTimesEvent(engine, 2).apply {
                    this.bounds.x = origin + 4f
                })
                addEvent(SimpleEvent(engine, {
                    resetMusic()
                    musicStartSec = tempos.beatsToSeconds(origin + 6f)
                    seekMusic()
                }).apply {
                    this.bounds.x = origin + 6f
                })
                // Practice group 2
                addEvent(PracticeGroupEvent(engine, secondPractice, secondPracticeEnd, 0f).apply {
                    this.bounds.x = origin + 6f
                    this.bounds.width = 16f
                })
            }

            // Practice group 1
            addEvent(PracticeGroupEvent(engine, firstPractice, firstPracticeEnd, 2f).apply {
                this.bounds.x = 1.5f
                this.bounds.width = 14f
            })
        }

        return engine
    }

    private fun createLongShortFastPractice(): Engine {
        val engine = Engine()
        engine.music = MusicData(Gdx.files.internal("music/practice.ogg"), engine).apply {
            this.music.setLooping(true)
        }

        /*
        Order of events:
        Let's practice the\n"long-short-fast" pattern.
        (For the purposes of this\ndemo, the long ball
        will jump ahead to\nspeed things up.)
        [long-short-fast x3]
        Great job!
        You're a pro at this.\nReady for the real thing?
         */

        val textBoxInst = EventRegistry.map.getValue("text_box")
        val deployInst = EventRegistry.map.getValue("deploy")

        // Populate
        with(engine) {
            playbackStart = 0f
            musicStartSec = TempoUtils.beatsToSeconds(4f, 154f)
            tempos.add(TempoChange(tempos, 0f, 154f, Swing.STRAIGHT, 0f))

            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-1"], true), textBoxInst).apply {
                this.bounds.x = 0f
                this.bounds.width = 1f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-2"], true), textBoxInst).apply {
                this.bounds.x = 1f
                this.bounds.width = 1f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-3"], true), textBoxInst).apply {
                this.bounds.x = 2f
                this.bounds.width = 1f
            })
            addEvent(XMoreTimesEvent(engine, 3).apply {
                this.bounds.x = 3f
            })
            
            val practice: PracticeGroupEvent.(Float) -> List<Event> = { offset ->
                listOf(
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 2f
                            this.bounds.x = offset - 18f
                        },
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 1f
                            this.bounds.x = offset - 1f
                        },
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 0.5f
                            this.bounds.x = offset + 7.5f
                        }
                )
            }
            
            addEvent(PracticeGroupEvent(engine, practice, {
                // Add music fade
                val origin = bounds.maxX + 2f
                musicVolumes.add(MusicVolumeChange(musicVolumes, bounds.maxX, 2f, 0))
                musicVolumes.add(MusicVolumeChange(musicVolumes, origin + 4f, 0f, 100))
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.lsf.stage2-1"], true), textBoxInst).apply {
                    this.bounds.x = origin + 1f
                    this.bounds.width = 0.5f
                })
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.lsf.stage2-2"], true), textBoxInst).apply {
                    this.bounds.x = origin + 2f
                    this.bounds.width = 0.5f
                })
                addEvent(SimpleEvent(engine, {
                    engine.playState = PlayState.STOPPED
                    main.preferences.putBoolean(PreferenceKeys.PRACTICE_COMPLETE_PREFIX + PracticeStage.LONG_SHORT_FAST.name, true).flush()
                }).apply {
                    this.bounds.x = origin + 4f
                })
            }, 1f).apply {
                this.bounds.width = 15f
                this.bounds.x = 4f
            })
            
        }

        return engine
    }

    private fun createSkillStarPractice(): Engine {
        val engine = Engine()
        engine.music = MusicData(Gdx.files.internal("music/practice.ogg"), engine).apply {
            this.music.setLooping(true)
        }

        /*
        Order of events:
        
         */

        val textBoxInst = EventRegistry.map.getValue("text_box")
        val deployInst = EventRegistry.map.getValue("deploy")

        // Populate
        with(engine) {
            playbackStart = 0f
            musicStartSec = TempoUtils.beatsToSeconds(4f, 154f)
            tempos.add(TempoChange(tempos, 0f, 154f, Swing.STRAIGHT, 0f))

            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-1"], true), textBoxInst).apply {
                this.bounds.x = 0f
                this.bounds.width = 1f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-2"], true), textBoxInst).apply {
                this.bounds.x = 1f
                this.bounds.width = 1f
            })
            addEvent(SpawnTextBoxEvent(this, TextBox(Localization["practice.lsf.stage1-3"], true), textBoxInst).apply {
                this.bounds.x = 2f
                this.bounds.width = 1f
            })
            addEvent(XMoreTimesEvent(engine, 3).apply {
                this.bounds.x = 3f
            })

            val practice: PracticeGroupEvent.(Float) -> List<Event> = { offset ->
                listOf(
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 2f
                            this.bounds.x = offset - 18f
                        },
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 1f
                            this.bounds.x = offset - 1f
                        },
                        DeployEvent(engine, deployInst).apply {
                            this.bounds.width = 0.5f
                            this.bounds.x = offset + 7.5f
                        }
                )
            }

            addEvent(PracticeGroupEvent(engine, practice, {
                // Add music fade
                val origin = bounds.maxX + 2f
                musicVolumes.add(MusicVolumeChange(musicVolumes, bounds.maxX, 2f, 0))
                musicVolumes.add(MusicVolumeChange(musicVolumes, origin + 4f, 0f, 100))
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.lsf.stage2-1"], true), textBoxInst).apply {
                    this.bounds.x = origin + 1f
                    this.bounds.width = 0.5f
                })
                addEvent(SpawnTextBoxEvent(engine, TextBox(Localization["practice.lsf.stage2-2"], true), textBoxInst).apply {
                    this.bounds.x = origin + 2f
                    this.bounds.width = 0.5f
                })
                addEvent(SimpleEvent(engine, {
                    engine.playState = PlayState.STOPPED
                    main.preferences.putBoolean(PreferenceKeys.PRACTICE_COMPLETE_PREFIX + PracticeStage.LONG_SHORT_FAST.name, true).flush()
                }).apply {
                    this.bounds.x = origin + 4f
                })
            }, 1f).apply {
                this.bounds.width = 15f
                this.bounds.x = 4f
            })

        }

        return engine
    }

    private val task: Deferred<Engine> = GlobalScope.async {
        when (practiceStage) {
            PracticeStage.STANDARD -> createStandardPractice()
            PracticeStage.LONG_SHORT_FAST -> createLongShortFastPractice()
            PracticeStage.SKILL_STAR -> createSkillStarPractice()
        }
    }

    @ExperimentalCoroutinesApi
    override fun renderUpdate() {
        super.renderUpdate()
        if (task.isCompleted) {
            val exception = task.getCompletionExceptionOrNull()
            if (exception != null) {
                exception.printStackTrace()
                main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), null, WipeFrom(Color.BLACK, 0.35f))
            } else {
                main.screen = TransitionScreen(main, main.screen, PracticeScreen(main, task.getCompleted()), null, WipeFrom(Color.BLACK, 0.35f))
            }
        } else if (task.isCancelled) {
            main.screen = TransitionScreen(main, main.screen, MainMenuScreen(main), null, WipeFrom(Color.BLACK, 0.35f))
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
