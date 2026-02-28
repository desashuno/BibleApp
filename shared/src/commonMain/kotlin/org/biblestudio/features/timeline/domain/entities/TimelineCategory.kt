package org.biblestudio.features.timeline.domain.entities

/**
 * Historical eras for categorising biblical timeline events.
 */
enum class TimelineCategory(val displayName: String, val colorHex: Long) {
    Creation("Creation", 0xFF8BC34A),
    Patriarchs("Patriarchs", 0xFF4CAF50),
    Exodus("Exodus", 0xFFFF9800),
    Judges("Judges", 0xFFFF5722),
    Kingdom("Kingdom", 0xFF2196F3),
    Exile("Exile", 0xFF9C27B0),
    Return("Return", 0xFF00BCD4),
    Intertestamental("Intertestamental", 0xFF607D8B),
    NewTestament("New Testament", 0xFFE91E63),
    EarlyChurch("Early Church", 0xFF795548);

    companion object {
        fun fromString(value: String): TimelineCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Patriarchs
    }
}
