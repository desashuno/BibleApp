package org.biblestudio.features.module_system.domain.entities

/**
 * Represents an installed Bible module (text, commentary, dictionary, etc.).
 *
 * @param id Auto-generated database ID.
 * @param uuid Unique identifier for the module.
 * @param name Full display name (e.g., "King James Version").
 * @param abbreviation Short code (e.g., "KJV").
 * @param language ISO 639-1 language code.
 * @param type Module type: "bible", "commentary", "dictionary", "devotional".
 * @param version Module version string.
 * @param sizeBytes Size of the module data in bytes.
 * @param description Human-readable description.
 * @param sourceType How the module was installed: "sword", "osis", "usfm", "zip", "local".
 * @param installedAt ISO 8601 timestamp of installation.
 * @param isActive Whether the module is active (not soft-deleted).
 */
data class InstalledModule(
    val id: Long,
    val uuid: String,
    val name: String,
    val abbreviation: String,
    val language: String,
    val type: String,
    val version: String,
    val sizeBytes: Long,
    val description: String,
    val sourceType: String,
    val installedAt: String,
    val isActive: Boolean
)
