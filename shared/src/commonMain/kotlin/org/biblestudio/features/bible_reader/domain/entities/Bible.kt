package org.biblestudio.features.bible_reader.domain.entities

/**
 * Represents a Bible version/translation.
 *
 * @param id Auto-generated database ID.
 * @param abbreviation Short code (e.g., "KJV", "ESV", "RVR60").
 * @param name Full display name (e.g., "King James Version").
 * @param language ISO 639-1 language code.
 * @param textDirection "ltr" or "rtl".
 */
data class Bible(
    val id: Long,
    val abbreviation: String,
    val name: String,
    val language: String = "en",
    val textDirection: String = "ltr"
)
