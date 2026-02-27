package org.biblestudio.features.exegetical_guide.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.exegetical_guide.data.mappers.toCommentaryEntry
import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry
import org.biblestudio.features.exegetical_guide.domain.repositories.CommentaryRepository
import org.biblestudio.features.resource_library.data.mappers.toResource
import org.biblestudio.features.resource_library.domain.entities.Resource

internal class CommentaryRepositoryImpl(
    private val database: BibleStudioDatabase
) : CommentaryRepository {

    override suspend fun getCommentaries(): Result<List<Resource>> = runCatching {
        database.resourceQueries
            .resourcesByType("commentary")
            .executeAsList()
            .map { it.toResource() }
    }

    override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<CommentaryEntry>> =
        runCatching {
            database.resourceQueries
                .entriesForVerse(resourceId, globalVerseId)
                .executeAsList()
                .map { it.toCommentaryEntry() }
        }

    override suspend fun search(query: String, maxResults: Long): Result<List<CommentaryEntry>> = runCatching {
        database.resourceQueries
            .searchResources(query, maxResults)
            .executeAsList()
            .map { it.toCommentaryEntry() }
    }
}
