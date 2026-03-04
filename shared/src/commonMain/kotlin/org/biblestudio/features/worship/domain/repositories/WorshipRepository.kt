package org.biblestudio.features.worship.domain.repositories

import org.biblestudio.features.worship.domain.entities.LyricLine
import org.biblestudio.features.worship.domain.entities.Playlist
import org.biblestudio.features.worship.domain.entities.Song

interface WorshipRepository {

    // Songs
    suspend fun getAllSongs(): Result<List<Song>>
    suspend fun getSongById(songId: Long): Result<Song?>
    suspend fun getSongsByGenre(genre: String): Result<List<Song>>
    suspend fun searchSongs(query: String, maxResults: Long = 50): Result<List<Song>>
    suspend fun deleteSong(songId: Long): Result<Unit>

    // Playlists
    suspend fun getAllPlaylists(): Result<List<Playlist>>
    suspend fun getPlaylistWithSongs(playlistId: String): Result<Playlist?>
    suspend fun createPlaylist(name: String): Result<Playlist>
    suspend fun updatePlaylistName(playlistId: String, name: String): Result<Unit>
    suspend fun deletePlaylist(playlistId: String): Result<Unit>
    suspend fun addSongToPlaylist(playlistId: String, songId: Long, position: Int): Result<Unit>
    suspend fun removeSongFromPlaylist(playlistId: String, songId: Long): Result<Unit>

    // Favorites
    suspend fun getFavorites(): Result<List<Song>>
    suspend fun isFavorite(songId: Long): Result<Boolean>
    suspend fun toggleFavorite(songId: Long): Result<Boolean>

    // History
    suspend fun getRecentHistory(maxResults: Long = 50): Result<List<Song>>
    suspend fun recordPlay(songId: Long): Result<Unit>
    suspend fun clearHistory(): Result<Unit>

    // Lyrics
    suspend fun getLyricLines(songId: Long): Result<List<LyricLine>>
    suspend fun getLyricLineAtTime(songId: Long, positionMs: Long): Result<LyricLine?>

    // Verse links
    suspend fun getSongsForVerse(globalVerseId: Long): Result<List<Song>>
    suspend fun getVerseLinksForSong(songId: Long): Result<List<Long>>
}
