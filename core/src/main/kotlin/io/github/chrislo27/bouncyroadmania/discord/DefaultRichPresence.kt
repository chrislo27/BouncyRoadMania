package io.github.chrislo27.bouncyroadmania.discord

import club.minnced.discord.rpc.DiscordRichPresence
import io.github.chrislo27.bouncyroadmania.BRMania
import io.github.chrislo27.bouncyroadmania.BRManiaApp


class DefaultRichPresence(state: String = "",
                          party: Pair<Int, Int> = DEFAULT_PARTY,
                          smallIcon: String = "",
                          smallIconText: String = state,
                          largeIcon: String? = null,
                          largeIconText: String? = null)
    : DiscordRichPresence() {

    companion object {
        val DEFAULT_PARTY: Pair<Int, Int> = 0 to 0
    }

    constructor(presenceState: PresenceState)
            : this(presenceState.state, presenceState.getPartyCount(), presenceState.smallIcon, presenceState.smallIconText,
                   presenceState.largeIcon, presenceState.largeIconText) {
        presenceState.modifyRichPresence(this)
    }

    init {
        details = if (BRMania.VERSION.suffix.startsWith("DEV")) {
            "Working on ${BRMania.VERSION.copy(suffix = "")}"
        } else if (BRMania.VERSION.suffix.startsWith("RC") || BRMania.VERSION.suffix.startsWith("SNAPSHOT")) {
            "Testing ${BRMania.VERSION}"
        } else {
            "Using ${BRMania.VERSION}"
        }
        startTimestamp = BRManiaApp.instance.startTimeMillis / 1000L // Epoch seconds
        largeImageKey = largeIcon ?: DiscordHelper.DEFAULT_LARGE_IMAGE
        largeImageText = largeIconText ?: "Bouncy Road Mania is a program that emulates the Bouncy Road minigame from the Rhythm Heaven series"
        smallImageKey = smallIcon
        smallImageText = smallIconText
        this.state = state
        if (party.first > 0 && party.second > 0) {
            partySize = party.first
            partyMax = party.second
        }
    }

}