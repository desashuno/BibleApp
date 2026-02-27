package org.biblestudio.features.settings.domain.entities

/**
 * A key-value application setting.
 *
 * @param key Unique setting key (e.g., "theme", "font_size").
 * @param value Serialized value as text.
 * @param type Value type hint (e.g., "string", "int", "boolean", "json").
 * @param category Grouping category for UI display (e.g., "display", "reading", "sync").
 */
data class AppSetting(
    val key: String,
    val value: String,
    val type: String,
    val category: String
)
