package org.biblestudio.features.worship.domain.entities

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList()
)
