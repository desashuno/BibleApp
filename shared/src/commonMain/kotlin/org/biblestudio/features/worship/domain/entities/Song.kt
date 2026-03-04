package org.biblestudio.features.worship.domain.entities

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val language: String,
    val durationMs: Long,
    val filePath: String,
    val coverArtPath: String,
    val trackNumber: Int,
    val year: Int,
    val isUserImport: Boolean
)
