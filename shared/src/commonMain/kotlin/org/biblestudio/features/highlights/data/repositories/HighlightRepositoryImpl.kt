package org.biblestudio.features.highlights.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.highlights.data.mappers.toHighlight
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository

internal class HighlightRepositoryImpl(
    private val database: BibleStudioDatabase
) : HighlightRepository {

    override suspend fun getHighlightsForVerse(globalVerseId: Long): Result<List<Highlight>> = runCatching {
        database.annotationQueries
            .highlightsByVerse(globalVerseId)
            .executeAsList()
            .map { it.toHighlight() }
    }

    override suspend fun getAll(): Result<List<Highlight>> = runCatching {
        database.annotationQueries
            .allHighlights()
            .executeAsList()
            .map { it.toHighlight() }
    }

    override suspend fun getHighlightsForVerseRange(startVerseId: Long, endVerseId: Long): Result<List<Highlight>> =
        runCatching {
            database.annotationQueries
                .highlightsByVerseRange(startVerseId, endVerseId)
                .executeAsList()
                .map { it.toHighlight() }
        }

    override suspend fun create(highlight: Highlight): Result<Unit> = runCatching {
        database.annotationQueries.insertHighlight(
            uuid = highlight.uuid,
            globalVerseId = highlight.globalVerseId,
            colorIndex = highlight.colorIndex,
            style = highlight.style,
            startOffset = highlight.startOffset,
            endOffset = highlight.endOffset,
            createdAt = highlight.createdAt,
            updatedAt = highlight.updatedAt,
            deviceId = highlight.deviceId
        )
    }

    override suspend fun update(highlight: Highlight): Result<Unit> = runCatching {
        database.annotationQueries.updateHighlight(
            uuid = highlight.uuid,
            colorIndex = highlight.colorIndex,
            style = highlight.style,
            startOffset = highlight.startOffset,
            endOffset = highlight.endOffset,
            updatedAt = highlight.updatedAt,
            deviceId = highlight.deviceId
        )
    }

    override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.annotationQueries.softDeleteHighlight(
            uuid = uuid,
            deletedAt = deletedAt
        )
    }

    override fun watchHighlightsForVerse(globalVerseId: Long): Flow<List<Highlight>> = database.annotationQueries
        .highlightsByVerse(globalVerseId)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toHighlight() } }
}
