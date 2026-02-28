package org.biblestudio.features.audio_sync.domain.entities

/**
 * Represents an audio recording of a Bible chapter.
 *
 * @param id Database primary key.
 * @param title Display title (e.g. "Genesis 1 – KJV Audio").
 * @param filePath Local or remote path to the audio file.
 * @param bibleId Identifier of the Bible translation.
 * @param bookNumber 1-indexed book number.
 * @param chapterNumber 1-indexed chapter number.
 * @param durationMs Duration of the audio in milliseconds.
 * @param narrator Optional narrator name.
 * @param language ISO 639-1 language code.
 */
data class AudioTrack(
    val id: Long,
    val title: String,
    val filePath: String,
    val bibleId: String,
    val bookNumber: Int,
    val chapterNumber: Int,
    val durationMs: Long,
    val narrator: String?,
    val language: String
)
