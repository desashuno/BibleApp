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
            meta("bible-reader", "Bible Reader", "book", PaneCategory.Text, "Read and navigate Bible text"),
            meta("text-comparison", "Text Comparison", "compare", PaneCategory.Text, "Compare versions"),
            meta("cross-references", "Cross References", "link", PaneCategory.Study, "Cross-references"),
            meta("word-study", "Word Study", "translate", PaneCategory.Study, "Original-language words"),
            meta("morphology", "Morphology", "code", PaneCategory.Study, "Grammatical forms"),
            meta("interlinear", "Interlinear", "view_column", PaneCategory.Study, "Interlinear text"),
            meta("passage-guide", "Passage Guide", "explore", PaneCategory.Study, "Passage analysis"),
            meta("exegetical-guide", "Exegetical Guide", "school", PaneCategory.Study, "Exegetical resources"),
            meta("commentary", "Commentary", "menu_book", PaneCategory.Resource, "Read commentaries"),
            meta("dictionary", "Dictionary", "library_books", PaneCategory.Resource, "Dictionary entries"),
            meta("resource-library", "Resource Library", "local_library", PaneCategory.Resource, "Browse resources"),
            meta("note-editor", "Notes", "edit_note", PaneCategory.Writing, "Study notes"),
            meta("sermon-editor", "Sermon Editor", "description", PaneCategory.Writing, "Draft sermons"),
            meta("highlights", "Highlights", "highlight", PaneCategory.Writing, "Manage highlights"),
            meta("bookmarks", "Bookmarks", "bookmark", PaneCategory.Writing, "Manage bookmarks"),
            meta("search", "Search", "search", PaneCategory.Tool, "Full-text search"),
            meta("knowledge-graph", "Knowledge Graph", "hub", PaneCategory.Tool, "Knowledge graph explorer"),
            meta("timeline", "Timeline", "timeline", PaneCategory.Tool, "Event timeline"),
            meta("theological-atlas", "Theological Atlas", "map", PaneCategory.Tool, "Geographic maps"),
            meta("reading-plans", "Reading Plans", "event_note", PaneCategory.Tool, "Reading plans"),
            meta("dashboard", "Dashboard", "dashboard", PaneCategory.Tool, "Study dashboard"),
            meta("audio-sync", "Audio Sync", "headphones", PaneCategory.Media, "Audio playback")
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
