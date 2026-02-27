package org.biblestudio.features.bookmarks_history.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry

/**
 * View mode toggle for the Bookmarks pane.
 */
enum class BookmarksViewMode { Bookmarks, History }

/**
 * Observable state for the Bookmarks & History pane.
 */
data class BookmarksState(
    val bookmarks: List<Bookmark> = emptyList(),
    val folders: List<BookmarkFolder> = emptyList(),
    val activeFolder: BookmarkFolder? = null,
    val history: List<HistoryEntry> = emptyList(),
    val viewMode: BookmarksViewMode = BookmarksViewMode.Bookmarks,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Bookmarks & History pane.
 */
interface BookmarksComponent {

    /** The current bookmarks/history state observable. */
    val state: StateFlow<BookmarksState>

    /** Navigate to a bookmarked verse. */
    fun onBookmarkTapped(bookmark: Bookmark)

    /** Open a folder. */
    fun onFolderSelected(uuid: String?)

    /** Create a new bookmark folder. */
    fun onCreateFolder(name: String, parentId: String?)

    /** Rename a folder. */
    fun onRenameFolder(uuid: String, newName: String)

    /** Delete a folder. */
    fun onDeleteFolder(uuid: String)

    /** Add a bookmark for a verse. */
    fun onAddBookmark(globalVerseId: Long, label: String)

    /** Delete a bookmark. */
    fun onDeleteBookmark(uuid: String)

    /** Navigate to a history entry verse. */
    fun onHistoryTapped(entry: HistoryEntry)

    /** Switch between Bookmarks and History tabs. */
    fun onViewModeChanged(mode: BookmarksViewMode)

    /** Clear all navigation history. */
    fun onClearHistory()
}
