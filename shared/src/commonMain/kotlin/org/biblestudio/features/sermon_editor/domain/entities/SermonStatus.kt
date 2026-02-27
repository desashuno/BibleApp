package org.biblestudio.features.sermon_editor.domain.entities

/**
 * Workflow status for a [Sermon].
 */
enum class SermonStatus(val raw: String) {
    Draft("draft"),
    InProgress("in_progress"),
    Ready("ready"),
    Delivered("delivered");

    companion object {
        fun fromString(value: String): SermonStatus = entries.firstOrNull { it.raw == value } ?: Draft
    }
}
