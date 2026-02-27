package org.biblestudio.features.resource_library.domain.entities

/**
 * Metadata for an imported resource (commentary, dictionary, etc.).
 *
 * @param uuid Unique identifier (UUID v4).
 * @param type Resource type (e.g., "commentary", "dictionary", "atlas").
 * @param title Display title.
 * @param author Author name.
 * @param version Version string of the resource.
 * @param format Data format (e.g., "json", "xml").
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the importing device.
 */
data class Resource(
    val uuid: String,
    val type: String,
    val title: String,
    val author: String,
    val version: String,
    val format: String,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
