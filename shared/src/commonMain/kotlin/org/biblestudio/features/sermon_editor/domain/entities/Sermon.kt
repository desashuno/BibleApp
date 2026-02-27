package org.biblestudio.features.sermon_editor.domain.entities

/**
 * Represents a sermon with metadata.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param title Sermon title.
 * @param scriptureRef Primary Scripture reference (e.g., "John 3:16-21").
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param status Workflow status (e.g., "draft", "review", "final").
 * @param deviceId Identifier of the device that last modified this sermon.
 */
data class Sermon(
    val uuid: String,
    val title: String,
    val scriptureRef: String,
    val createdAt: String,
    val updatedAt: String,
    val status: String,
    val deviceId: String
)
