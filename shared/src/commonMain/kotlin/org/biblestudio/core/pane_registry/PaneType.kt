package org.biblestudio.core.pane_registry

/**
 * Canonical pane type identifiers used across the codebase.
 *
 * Every component that references a pane type string should use these
 * constants instead of inline string literals.
 */
object PaneType {
    const val BIBLE_READER = "bible-reader"
    const val TEXT_COMPARISON = "text-comparison"
    const val CROSS_REFERENCES = "cross-references"
    const val WORD_STUDY = "word-study"
    const val MORPHOLOGY = "morphology"
    const val INTERLINEAR = "interlinear"
    const val PASSAGE_GUIDE = "passage-guide"
    const val EXEGETICAL_GUIDE = "exegetical-guide"
    const val COMMENTARY = "commentary"
    const val DICTIONARY = "dictionary"
    const val RESOURCE_LIBRARY = "resource-library"
    const val NOTE_EDITOR = "note-editor"
    const val SERMON_EDITOR = "sermon-editor"
    const val HIGHLIGHTS = "highlights"
    const val BOOKMARKS = "bookmarks"
    const val SEARCH = "search"
    const val KNOWLEDGE_GRAPH = "knowledge-graph"
    const val TIMELINE = "timeline"
    const val THEOLOGICAL_ATLAS = "theological-atlas"
    const val READING_PLANS = "reading-plans"
    const val DASHBOARD = "dashboard"
    const val AUDIO_SYNC = "audio-sync"
    const val SETTINGS = "settings"

    /** Alias: syntax-search renders with the Search component. */
    const val SYNTAX_SEARCH = "syntax-search"

    const val WORSHIP = "worship"

    /** Alias: module-manager is unified with resource-library. */
    const val MODULE_MANAGER = "module-manager"
}
