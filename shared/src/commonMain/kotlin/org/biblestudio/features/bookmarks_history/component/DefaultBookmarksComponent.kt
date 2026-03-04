package org.biblestudio.features.bookmarks_history.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.util.nowIso
import org.biblestudio.core.AppConstants
import org.biblestudio.core.util.generateUuid
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository

/**
 * Default [BookmarksComponent] managing bookmarks, folders, and history.
 */
@Suppress("TooManyFunctions")
internal class DefaultBookmarksComponent(
    componentContext: ComponentContext,
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: HistoryRepository,
    private val verseBus: VerseBus
) : BookmarksComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(BookmarksState())
    override val state: StateFlow<BookmarksState> = _state.asStateFlow()

    init {
        loadBookmarks()
        loadFolders()
        loadHistory()
        observeVerseBus()
    }

    override fun onBookmarkTapped(bookmark: Bookmark) {
        verseBus.publish(LinkEvent.VerseSelected(bookmark.globalVerseId.toInt()))
    }

    override fun onFolderSelected(uuid: String?) {
        val folder = if (uuid != null) {
            _state.value.folders.firstOrNull { it.uuid == uuid }
        } else {
            null
        }
        _state.update { it.copy(activeFolder = folder) }
        if (uuid != null) {
            scope.launch {
                bookmarkRepository.getByFolder(uuid)
                    .onSuccess { bookmarks ->
                        _state.update { it.copy(bookmarks = bookmarks) }
                    }
            }
        } else {
            loadBookmarks()
        }
    }

    override fun onCreateFolder(name: String, parentId: String?) {
        val now = nowIso()
        val folder = BookmarkFolder(
            uuid = generateUuid(),
            name = name,
            parentId = parentId,
            sortOrder = _state.value.folders.size.toLong(),
            createdAt = now,
            updatedAt = now,
            deviceId = AppConstants.DEVICE_ID_LOCAL
        )
        scope.launch {
            bookmarkRepository.createFolder(folder)
                .onSuccess { loadFolders() }
                .onFailure { e -> Napier.e("Failed to create folder", e) }
        }
    }

    override fun onRenameFolder(uuid: String, newName: String) {
        val existing = _state.value.folders.firstOrNull { it.uuid == uuid } ?: return
        val now = nowIso()
        val updated = existing.copy(name = newName, updatedAt = now)
        scope.launch {
            bookmarkRepository.updateFolder(updated)
                .onSuccess { loadFolders() }
        }
    }

    override fun onDeleteFolder(uuid: String) {
        val now = nowIso()
        scope.launch {
            bookmarkRepository.deleteFolder(uuid, now)
                .onSuccess { loadFolders() }
        }
    }

    override fun onAddBookmark(globalVerseId: Long, label: String) {
        val now = nowIso()
        val bookmark = Bookmark(
            uuid = generateUuid(),
            globalVerseId = globalVerseId,
            label = label,
            folderId = _state.value.activeFolder?.uuid,
            sortOrder = _state.value.bookmarks.size.toLong(),
            createdAt = now,
            updatedAt = now,
            deviceId = AppConstants.DEVICE_ID_LOCAL
        )
        scope.launch {
            bookmarkRepository.create(bookmark)
                .onSuccess { loadBookmarks() }
                .onFailure { e -> Napier.e("Failed to create bookmark", e) }
        }
    }

    override fun onDeleteBookmark(uuid: String) {
        val now = nowIso()
        scope.launch {
            bookmarkRepository.delete(uuid, now)
                .onSuccess { loadBookmarks() }
        }
    }

    override fun onHistoryTapped(entry: HistoryEntry) {
        verseBus.publish(LinkEvent.VerseSelected(entry.globalVerseId.toInt()))
    }

    override fun onViewModeChanged(mode: BookmarksViewMode) {
        _state.update { it.copy(viewMode = mode) }
        if (mode == BookmarksViewMode.History) {
            loadHistory()
        }
    }

    override fun onClearHistory() {
        scope.launch {
            historyRepository.clear()
                .onSuccess { _state.update { it.copy(history = emptyList()) } }
        }
    }

    private fun loadBookmarks() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            bookmarkRepository.getAll()
                .onSuccess { bookmarks ->
                    _state.update { it.copy(bookmarks = bookmarks, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load bookmarks", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadFolders() {
        scope.launch {
            bookmarkRepository.getAllFolders()
                .onSuccess { folders ->
                    _state.update { it.copy(folders = folders) }
                }
        }
    }

    private fun loadHistory() {
        scope.launch {
            historyRepository.getHistory()
                .onSuccess { entries ->
                    _state.update { it.copy(history = entries) }
                }
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    historyRepository.addEntry(event.globalVerseId.toLong())
                }
        }
    }

}
