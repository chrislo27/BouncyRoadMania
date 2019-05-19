package io.github.chrislo27.bouncyroadmania.discord


sealed class PresenceState(open val state: String = "", open val smallIcon: String = "", open val smallIconText: String = state,
                           open val largeIcon: String? = null, open val largeIconText: String? = null) {

    open fun getPartyCount(): Pair<Int, Int> = DefaultRichPresence.DEFAULT_PARTY

    open fun modifyRichPresence(richPresence: DefaultRichPresence) {
    }

    // ---------------- IMPLEMENTATIONS BELOW ----------------

    object Loading
        : PresenceState("Loading...")

    object MainMenu
        : PresenceState("In Main Menu")
    
    object Editing
        : PresenceState("Editing a project")
    
    object Playing
        : PresenceState("Playing")
    
    object GameSelect
        : PresenceState("Deciding what to play...")

}
