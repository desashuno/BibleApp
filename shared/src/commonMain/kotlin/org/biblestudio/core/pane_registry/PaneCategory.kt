package org.biblestudio.core.pane_registry

/**
 * Logical grouping for pane types, used for activity-bar icons
 * and the "Add Pane" picker.
 */
enum class PaneCategory {
    /** Bible text / reading panes. */
    Text,

    /** Study tools: word study, morphology, cross-references. */
    Study,

    /** External resources: commentaries, dictionaries, library. */
    Resource,

    /** Writing tools: notes, sermon editor. */
    Writing,

    /** Utility / meta panes: search, settings, knowledge graph. */
    Tool,

    /** Audio/video synchronization panes. */
    Media
}
