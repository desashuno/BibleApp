package org.biblestudio.features.worship.data.mappers

import migrations.Worship_lyric_lines
import migrations.Worship_songs
import org.biblestudio.database.RecentHistory
import org.biblestudio.features.worship.domain.entities.LyricLine
import org.biblestudio.features.worship.domain.entities.Song

internal fun Worship_songs.toSong(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    genre = genre,
    language = language,
    durationMs = duration_ms,
    filePath = file_path,
    coverArtPath = cover_art_path,
    trackNumber = track_number.toInt(),
    year = year.toInt(),
    isUserImport = is_user_import != 0L
)

internal fun RecentHistory.toSong(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    genre = genre,
    language = language,
    durationMs = duration_ms,
    filePath = file_path,
    coverArtPath = cover_art_path,
    trackNumber = track_number.toInt(),
    year = year.toInt(),
    isUserImport = is_user_import != 0L
)

internal fun Worship_lyric_lines.toLyricLine(): LyricLine = LyricLine(
    lineIndex = line_index.toInt(),
    startMs = start_ms,
    endMs = end_ms,
    text = text
)
