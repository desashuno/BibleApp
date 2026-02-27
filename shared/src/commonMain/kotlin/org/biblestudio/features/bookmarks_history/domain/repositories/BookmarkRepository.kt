package org.biblestudio.features.bookmarks_history.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder

/**
 * CRUD operations for verse bookmarks and bookmark folders.
 */
interface BookmarkRepository {

    /** Returns bookmarks for a specific verse. */
    suspend fun getBookmarksForVerse(globalVerseId: Long): Result<List<Bookmark>>

    /** Returns bookmarks in a specific folder, ordered by sort order. */
    suspend fun getByFolder(folderId: String): Result<List<Bookmark>>

    /** Returns all bookmarks, ordered by sort order. */
    suspend fun getAll(): Result<List<Bookmark>>

    /** Creates a new bookmark. */
    suspend fun create(bookmark: Bookmark): Result<Unit>

    /** Updates an existing bookmark. */
    suspend fun update(bookmark: Bookmark): Result<Unit>

    /** Soft-deletes a bookmark by UUID. */
    suspend fun delete(uuid: String, deletedAt: String): Result<Unit>

    /** Reactive stream of all bookmarks. */
    fun watchAll(): Flow<List<Bookmark>>

    // ── Folder operations ───────────────────────────────────────

    /** Returns all folders, excluding soft-deleted. */
    suspend fun getAllFolders(): Result<List<BookmarkFolder>>

    /** Returns sub-folders of a parent. */
    suspend fun getFoldersByParent(parentId: String): Result<List<BookmarkFolder>>

    /** Returns root-level folders (no parent). */
    suspend fun getRootFolders(): Result<List<BookmarkFolder>>

    /** Creates a new folder. */
    suspend fun createFolder(folder: BookmarkFolder): Result<Unit>

    /** Updates an existing folder. */
    suspend fun updateFolder(folder: BookmarkFolder): Result<Unit>

    /** Soft-deletes a folder by UUID. */
    suspend fun deleteFolder(uuid: String, deletedAt: String): Result<Unit>
}
