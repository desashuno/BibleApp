package org.biblestudio.features.audio_sync.data.mappers

import migrations.Audio_sync_points
import migrations.Audio_tracks
import org.biblestudio.features.audio_sync.domain.entities.AudioSyncPoint
import org.biblestudio.features.audio_sync.domain.entities.AudioTrack

internal fun Audio_tracks.toAudioTrack(): AudioTrack = AudioTrack(
    id = id,
    title = title,
    filePath = file_path,
    bibleId = bible_id,
    bookNumber = book_number.toInt(),
    chapterNumber = chapter_number.toInt(),
    durationMs = duration_ms,
    narrator = narrator,
    language = language
)

internal fun Audio_sync_points.toAudioSyncPoint(): AudioSyncPoint = AudioSyncPoint(
    id = id,
    trackId = track_id,
    globalVerseId = global_verse_id,
    startMs = start_ms,
    endMs = end_ms
)
