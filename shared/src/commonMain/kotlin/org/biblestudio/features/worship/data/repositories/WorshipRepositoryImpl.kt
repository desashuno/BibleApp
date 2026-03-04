package org.biblestudio.features.worship.data.repositories

import org.biblestudio.core.util.generateUuid
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.worship.data.mappers.toLyricLine
import org.biblestudio.features.worship.data.mappers.toSong
import org.biblestudio.features.worship.domain.entities.LyricLine
import org.biblestudio.features.worship.domain.entities.Playlist
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.features.worship.domain.repositories.WorshipRepository

@Suppress("TooManyFunctions")
internal class WorshipRepositoryImpl(
    private val database: BibleStudioDatabase
) : WorshipRepository {

    private val q get() = database.worshipQueries

    override suspend fun getAllSongs(): Result<List<Song>> = runCatching {
        q.allSongs().executeAsList().map { it.toSong() }
    }

    override suspend fun getSongById(songId: Long): Result<Song?> = runCatching {
        q.songById(songId).executeAsOneOrNull()?.toSong()
    }

    override suspend fun getSongsByGenre(genre: String): Result<List<Song>> = runCatching {
        q.songsByGenre(genre).executeAsList().map { it.toSong() }
    }

    override suspend fun searchSongs(query: String, maxResults: Long): Result<List<Song>> = runCatching {
        val ftsResults = q.searchSongsFts(query, maxResults).executeAsList()
        if (ftsResults.isNotEmpty()) {
            ftsResults.map { it.toSong() }
        } else {
            q.searchSongsFallback(query, maxResults).executeAsList().map { it.toSong() }
        }
    }

    override suspend fun deleteSong(songId: Long): Result<Unit> = runCatching {
        q.deleteSong(songId)
    }

    override suspend fun getAllPlaylists(): Result<List<Playlist>> = runCatching {
        q.allPlaylists().executeAsList().map { row ->
            Playlist(id = row.id, name = row.name)
        }
    }

    override suspend fun getPlaylistWithSongs(playlistId: String): Result<Playlist?> = runCatching {
        val row = q.playlistById(playlistId).executeAsOneOrNull() ?: return@runCatching null
        val songs = q.songsForPlaylist(playlistId).executeAsList().map { it.toSong() }
        Playlist(id = row.id, name = row.name, songs = songs)
    }

    override suspend fun createPlaylist(name: String): Result<Playlist> = runCatching {
        val id = generateUuid()
        q.insertPlaylist(id, name)
        Playlist(id = id, name = name)
    }

    override suspend fun updatePlaylistName(playlistId: String, name: String): Result<Unit> = runCatching {
        q.updatePlaylistName(name = name, id = playlistId)
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> = runCatching {
        q.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(
        playlistId: String,
        songId: Long,
        position: Int
    ): Result<Unit> = runCatching {
        q.addSongToPlaylist(playlistId, songId, position.toLong())
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: Long): Result<Unit> = runCatching {
        q.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun getFavorites(): Result<List<Song>> = runCatching {
        q.allFavorites().executeAsList().map { it.toSong() }
    }

    override suspend fun isFavorite(songId: Long): Result<Boolean> = runCatching {
        q.isFavorite(songId).executeAsOne()
    }

    override suspend fun toggleFavorite(songId: Long): Result<Boolean> = runCatching {
        val isFav = q.isFavorite(songId).executeAsOne()
        if (isFav) {
            q.removeFavorite(songId)
            false
        } else {
            q.addFavorite(songId)
            true
        }
    }

    override suspend fun getRecentHistory(maxResults: Long): Result<List<Song>> = runCatching {
        q.recentHistory(maxResults).executeAsList().map { it.toSong() }
    }

    override suspend fun recordPlay(songId: Long): Result<Unit> = runCatching {
        q.insertPlayHistory(songId)
    }

    override suspend fun clearHistory(): Result<Unit> = runCatching {
        q.clearPlayHistory()
    }

    override suspend fun getLyricLines(songId: Long): Result<List<LyricLine>> = runCatching {
        q.lyricLinesForSong(songId).executeAsList().map { it.toLyricLine() }
    }

    override suspend fun getLyricLineAtTime(songId: Long, positionMs: Long): Result<LyricLine?> = runCatching {
        q.lyricLineAtTime(songId, positionMs).executeAsOneOrNull()?.toLyricLine()
    }

    override suspend fun getSongsForVerse(globalVerseId: Long): Result<List<Song>> = runCatching {
        q.songsForVerse(globalVerseId).executeAsList().map { it.toSong() }
    }

    override suspend fun getVerseLinksForSong(songId: Long): Result<List<Long>> = runCatching {
        q.verseLinksForSong(songId).executeAsList().map { it.global_verse_id }
    }
}
