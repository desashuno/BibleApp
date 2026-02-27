package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.bookmarks_history.component.BookmarksState
import org.biblestudio.features.bookmarks_history.component.BookmarksViewMode
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry

@OptIn(ExperimentalTestApi::class)
class BookmarksPaneTest {

    private val testBookmark = Bookmark(
        uuid = "bm-1",
        globalVerseId = 1_001_001,
        label = "Genesis 1:1",
        folderId = null,
        sortOrder = 0,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "dev-1"
    )

    private val testHistory = HistoryEntry(
        id = 1,
        globalVerseId = 2_001_001,
        visitedAt = "2025-06-01T12:00:00Z"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BookmarksPane_rendersBookmarksList() = runComposeUiTest {
        val flow = MutableStateFlow(
            BookmarksState(
                bookmarks = listOf(testBookmark),
                viewMode = BookmarksViewMode.Bookmarks,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BookmarksPane(
                stateFlow = flow,
                onBookmarkTapped = {},
                onFolderSelected = {},
                onCreateFolder = { _, _ -> },
                onDeleteBookmark = {},
                onDeleteFolder = {},
                onHistoryTapped = {},
                onViewModeChanged = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("Bookmarks").assertIsDisplayed()
        onNodeWithText("Genesis 1:1").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BookmarksPane_switchesToHistoryTab() = runComposeUiTest {
        var selectedMode: BookmarksViewMode? = null

        val flow = MutableStateFlow(
            BookmarksState(
                history = listOf(testHistory),
                viewMode = BookmarksViewMode.History,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BookmarksPane(
                stateFlow = flow,
                onBookmarkTapped = {},
                onFolderSelected = {},
                onCreateFolder = { _, _ -> },
                onDeleteBookmark = {},
                onDeleteFolder = {},
                onHistoryTapped = {},
                onViewModeChanged = { selectedMode = it },
                onClearHistory = {}
            )
        }

        onNodeWithText("History").assertIsDisplayed()
        onNodeWithText("Navigation History").assertIsDisplayed()
        onNodeWithText("Verse 2001001").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BookmarksPane_tapBookmarkTriggersCallback() = runComposeUiTest {
        var tappedBookmark: Bookmark? = null

        val flow = MutableStateFlow(
            BookmarksState(
                bookmarks = listOf(testBookmark),
                viewMode = BookmarksViewMode.Bookmarks,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BookmarksPane(
                stateFlow = flow,
                onBookmarkTapped = { tappedBookmark = it },
                onFolderSelected = {},
                onCreateFolder = { _, _ -> },
                onDeleteBookmark = {},
                onDeleteFolder = {},
                onHistoryTapped = {},
                onViewModeChanged = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("Genesis 1:1").performClick()
        assertEquals("bm-1", tappedBookmark?.uuid)
    }
}
