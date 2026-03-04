package org.biblestudio.core.pane_registry

import io.github.aakira.napier.Napier

/**
 * Central registry of all pane types available in the workspace.
 *
 * Each pane type is registered once (typically at app start via [init]) with a
 * [PaneBuilder] that can render it and [PaneMetadata] describing it.
 *
 * Thread-safety note: registration happens once at startup on the main thread
 * before any concurrent access, so no synchronization is needed.
 */
object PaneRegistry {

    private val builders = mutableMapOf<String, PaneBuilder>()
    private val metadataMap = mutableMapOf<String, PaneMetadata>()

    /** All currently registered pane type keys. */
    val availableTypes: Set<String>
        get() = builders.keys.toSet()

    /**
     * Registers a pane type with its [builder] and [metadata].
     * If the type is already registered it is silently replaced.
     */
    fun register(key: String, metadata: PaneMetadata, builder: PaneBuilder) {
        builders[key] = builder
        metadataMap[key] = metadata
        Napier.d("PaneRegistry: registered '$key'")
    }

    /**
     * Builds (invokes) the pane for the given [type] with the supplied [config].
     *
     * @throws IllegalArgumentException if [type] has not been registered.
     */
    fun build(type: String, config: Map<String, String> = emptyMap()) {
        val builder = builders[type]
            ?: throw IllegalArgumentException("Unknown pane type: '$type'")
        builder(config)
    }

    /**
     * Returns the [PaneMetadata] for a registered pane type.
     *
     * @throws IllegalArgumentException if [type] has not been registered.
     */
    fun metadata(type: String): PaneMetadata {
        return metadataMap[type]
            ?: throw IllegalArgumentException("Unknown pane type: '$type'")
    }

    /**
     * Registers placeholder builders for all 21 pane types.
     *
     * Called once at application startup. Each placeholder simply logs
     * a message; real implementations are registered later by each feature module.
     */
    /**
     * Registers placeholder builders for all 21 pane types.
     *
     * Called once at application startup. Each placeholder simply logs
     * a message; real implementations are registered later by each feature module.
     */
    @Suppress("LongMethod")
    fun init() {
        val panes = listOf(
            meta(PaneType.BIBLE_READER, "Bible Reader", "auto_stories", PaneCategory.Text, "Read and navigate Bible text"),
            meta(PaneType.TEXT_COMPARISON, "Text Comparison", "compare", PaneCategory.Text, "Compare versions"),
            meta(PaneType.CROSS_REFERENCES, "Cross References", "call_split", PaneCategory.Study, "Cross-references"),
            meta(PaneType.WORD_STUDY, "Word Study", "abc", PaneCategory.Study, "Original-language words"),
            meta(PaneType.MORPHOLOGY, "Morphology", "data_object", PaneCategory.Study, "Grammatical forms"),
            meta(PaneType.INTERLINEAR, "Interlinear", "view_column", PaneCategory.Study, "Interlinear text"),
            meta(PaneType.PASSAGE_GUIDE, "Passage Guide", "lightbulb", PaneCategory.Study, "Passage analysis"),
            meta(PaneType.EXEGETICAL_GUIDE, "Exegetical Guide", "history_edu", PaneCategory.Study, "Exegetical resources"),
            meta(PaneType.COMMENTARY, "Commentary", "rate_review", PaneCategory.Resource, "Read commentaries"),
            meta(PaneType.DICTIONARY, "Dictionary", "find_in_page", PaneCategory.Resource, "Dictionary entries"),
            meta(PaneType.RESOURCE_LIBRARY, "Resource Library", "local_library", PaneCategory.Resource, "Browse resources"),
            meta(PaneType.NOTE_EDITOR, "Notes", "edit_note", PaneCategory.Writing, "Study notes"),
            meta(PaneType.SERMON_EDITOR, "Sermon Editor", "article", PaneCategory.Writing, "Draft sermons"),
            meta(PaneType.HIGHLIGHTS, "Highlights", "format_color_fill", PaneCategory.Writing, "Manage highlights"),
            meta(PaneType.BOOKMARKS, "Bookmarks", "bookmark", PaneCategory.Writing, "Manage bookmarks"),
            meta(PaneType.SEARCH, "Search", "search", PaneCategory.Tool, "Full-text search"),
            meta(PaneType.KNOWLEDGE_GRAPH, "Knowledge Graph", "hub", PaneCategory.Tool, "Knowledge graph explorer"),
            meta(PaneType.TIMELINE, "Timeline", "timeline", PaneCategory.Tool, "Event timeline"),
            meta(PaneType.THEOLOGICAL_ATLAS, "Theological Atlas", "public", PaneCategory.Tool, "Geographic maps"),
            meta(PaneType.READING_PLANS, "Reading Plans", "calendar_month", PaneCategory.Tool, "Reading plans"),
            meta(PaneType.DASHBOARD, "Dashboard", "space_dashboard", PaneCategory.Tool, "Study dashboard"),
            meta(PaneType.AUDIO_SYNC, "Audio Sync", "headphones", PaneCategory.Media, "Audio playback"),
            meta(PaneType.WORSHIP, "Worship", "music_note", PaneCategory.Media, "Worship music player")
        )

        for (meta in panes) {
            register(meta.type, meta) { config ->
                Napier.w("Placeholder pane '${meta.type}' built with config=$config")
            }
        }

        Napier.i("PaneRegistry: initialized ${availableTypes.size} pane types")
    }

    /**
     * Removes all registered pane types. Used in tests.
     */
    fun clear() {
        builders.clear()
        metadataMap.clear()
    }

    private fun meta(
        type: String,
        displayName: String,
        icon: String,
        category: PaneCategory,
        description: String
    ): PaneMetadata = PaneMetadata(type, displayName, icon, category, description)
}
