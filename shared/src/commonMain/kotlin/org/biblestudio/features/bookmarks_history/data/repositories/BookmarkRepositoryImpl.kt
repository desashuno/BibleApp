package org.biblestudio.features.bookmarks_history.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bookmarks_history.data.mappers.toBookmark
import org.biblestudio.features.bookmarks_history.data.mappers.toBookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository

internal class BookmarkRepositoryImpl(
    private val database: BibleStudioDatabase
) : BookmarkRepository {

    override suspend fun getBookmarksForVerse(globalVerseId: Long): Result<List<Bookmark>> = runCatching {
        database.annotationQueries
            .bookmarksByVerse(globalVerseId)
            .executeAsList()
            .map { it.toBookmark() }
    }

    override suspend fun getByFolder(folderId: String): Result<List<Bookmark>> = runCatching {
        database.annotationQueries
            .bookmarksByFolder(folderId)
            .executeAsList()
            .map { it.toBookmark() }
    }

    override suspend fun getAll(): Result<List<Bookmark>> = runCatching {
        database.annotationQueries
            .allBookmarks()
            .executeAsList()
            .map { it.toBookmark() }
    }

    override suspend fun create(bookmark: Bookmark): Result<Unit> = runCatching {
        database.annotationQueries.insertBookmark(
            uuid = bookmark.uuid,
            globalVerseId = bookmark.globalVerseId,
            label = bookmark.label,
            folderId = bookmark.folderId,
            sortOrder = bookmark.sortOrder,
            createdAt = bookmark.createdAt,
            updatedAt = bookmark.updatedAt,
            deviceId = bookmark.deviceId
        )
    }

    override suspend fun update(bookmark: Bookmark): Result<Unit> = runCatching {
        database.annotationQueries.updateBookmark(
            uuid = bookmark.uuid,
            label = bookmark.label,
            folderId = bookmark.folderId,
            sortOrder = bookmark.sortOrder,
            updatedAt = bookmark.updatedAt,
            deviceId = bookmark.deviceId
        )
    }

    override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.annotationQueries.softDeleteBookmark(
            uuid = uuid,
            deletedAt = deletedAt
        )
    }

    override fun watchAll(): Flow<List<Bookmark>> = database.annotationQueries
        .allBookmarks()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toBookmark() } }

    // ── Folder operations ───────────────────────────────────────

    override suspend fun getAllFolders(): Result<List<BookmarkFolder>> = runCatching {
        database.annotationQueries
            .allFolders()
            .executeAsList()
            .map { it.toBookmarkFolder() }
    }

    override suspend fun getFoldersByParent(parentId: String): Result<List<BookmarkFolder>> = runCatching {
        database.annotationQueries
            .foldersByParent(parentId)
            .executeAsList()
            .map { it.toBookmarkFolder() }
    }

    override suspend fun getRootFolders(): Result<List<BookmarkFolder>> = runCatching {
        database.annotationQueries
            .rootFolders()
            .executeAsList()
            .map { it.toBookmarkFolder() }
    }

    override suspend fun createFolder(folder: BookmarkFolder): Result<Unit> = runCatching {
        database.annotationQueries.insertFolder(
            uuid = folder.uuid,
            name = folder.name,
            parentId = folder.parentId,
            sortOrder = folder.sortOrder,
            createdAt = folder.createdAt,
            updatedAt = folder.updatedAt,
            deviceId = folder.deviceId
        )
    }

    override suspend fun updateFolder(folder: BookmarkFolder): Result<Unit> = runCatching {
        database.annotationQueries.updateFolder(
            uuid = folder.uuid,
            name = folder.name,
            parentId = folder.parentId,
            sortOrder = folder.sortOrder,
            updatedAt = folder.updatedAt,
            deviceId = folder.deviceId
        )
    }

    override suspend fun deleteFolder(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.annotationQueries.softDeleteFolder(
            uuid = uuid,
            deletedAt = deletedAt
        )
    }
}
