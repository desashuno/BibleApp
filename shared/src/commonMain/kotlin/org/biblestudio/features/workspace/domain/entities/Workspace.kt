package org.biblestudio.features.workspace.domain.entities

/**
 * Represents a named workspace that the user can switch between.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param name User-visible workspace name.
 * @param isActive Whether this workspace is currently active.
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the device that last modified this workspace.
 */
data class Workspace(
    val uuid: String,
    val name: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
