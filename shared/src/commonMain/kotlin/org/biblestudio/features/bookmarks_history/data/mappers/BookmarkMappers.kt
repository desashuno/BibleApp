package org.biblestudio.features.bookmarks_history.data.mappers

import migrations.Bookmark_folders
import migrations.Bookmarks
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder

internal fun Bookmarks.toBookmark(): Bookmark = Bookmark(
    uuid = uuid,
    globalVerseId = global_verse_id,
    label = label,
    folderId = folder_id,
    sortOrder = sort_order,
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)

internal fun Bookmark_folders.toBookmarkFolder(): BookmarkFolder = BookmarkFolder(
    uuid = uuid,
    name = name,
    parentId = parent_id,
    sortOrder = sort_order,
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)
