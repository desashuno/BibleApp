package org.biblestudio.features.resource_library.domain.entities

/**
 * Taxonomy of resource types available in the library.
 */
enum class ResourceType(val dbValue: String) {
    Commentary("commentary"),
    Dictionary("dictionary"),
    Encyclopedia("encyclopedia"),
    DevotionalBook("devotional");

    companion object {
        fun fromString(value: String): ResourceType =
            entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
                ?: Commentary
    }
}
