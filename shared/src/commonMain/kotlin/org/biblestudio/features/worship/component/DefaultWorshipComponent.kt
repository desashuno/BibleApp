package org.biblestudio.features.worship.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.util.componentScope
import org.biblestudio.features.worship.WorshipPlayer
import org.biblestudio.features.worship.domain.entities.Playlist
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.features.worship.domain.entities.WorshipCategory
import org.biblestudio.features.worship.domain.repositories.WorshipRepository

/**
 * Per-pane component managing library browsing, search, playlists, and favorites.
 * Transport controls are delegated to [WorshipPlayer].
 */
@Suppress("TooManyFunctions")
internal class DefaultWorshipComponent(
    componentContext: ComponentContext,
    private val repository: WorshipRepository,
    private val player: WorshipPlayer
) : WorshipComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val _state = MutableStateFlow(WorshipState())
    override val state: StateFlow<WorshipState> = _state.asStateFlow()

    init {
        loadLibrary()
    }

    override fun onTabSelected(tab: WorshipTab) {
        _state.update { it.copy(activeTab = tab) }
        when (tab) {
            WorshipTab.Library -> loadLibrary()
            WorshipTab.Playlists -> loadPlaylists()
            WorshipTab.Favorites -> loadFavorites()
            WorshipTab.Queue -> { /* queue comes from WorshipPlayer state */ }
            WorshipTab.History -> loadHistory()
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadLibrary()
        } else {
            searchSongs(query)
        }
    }

    override fun onCategorySelected(category: WorshipCategory) {
        _state.update { it.copy(selectedCategory = category) }
        if (category == WorshipCategory.All) {
            loadLibrary()
        } else {
            loadByGenre(category.name)
        }
    }

    override fun onSongSelected(song: Song) {
        player.play(song, _state.value.songs)
    }

    override fun onPlayAll() {
        val songs = _state.value.songs
        if (songs.isNotEmpty()) {
            player.play(songs.first(), songs)
        }
    }

    override fun onToggleFavorite(songId: Long) {
        scope.launch {
            repository.toggleFavorite(songId)
                .onSuccess { loadFavorites() }
                .onFailure { e -> Napier.e("Failed to toggle favorite", e) }
        }
    }

    override fun onPlaylistSelected(playlist: Playlist) {
        scope.launch {
            repository.getPlaylistWithSongs(playlist.id)
                .onSuccess { full ->
                    _state.update { it.copy(selectedPlaylist = full) }
                }
                .onFailure { e -> Napier.e("Failed to load playlist", e) }
        }
    }

    override fun onCreatePlaylist(name: String) {
        scope.launch {
            repository.createPlaylist(name)
                .onSuccess { loadPlaylists() }
                .onFailure { e -> Napier.e("Failed to create playlist", e) }
        }
    }

    override fun onDeletePlaylist(playlistId: String) {
        scope.launch {
            repository.deletePlaylist(playlistId)
                .onSuccess {
                    _state.update { it.copy(selectedPlaylist = null) }
                    loadPlaylists()
                }
                .onFailure { e -> Napier.e("Failed to delete playlist", e) }
        }
    }

    override fun onAddSongToPlaylist(playlistId: String, songId: Long) {
        scope.launch {
            val position = _state.value.selectedPlaylist?.songs?.size ?: 0
            repository.addSongToPlaylist(playlistId, songId, position)
                .onSuccess { onPlaylistSelected(Playlist(id = playlistId, name = "")) }
                .onFailure { e -> Napier.e("Failed to add song to playlist", e) }
        }
    }

    override fun onClearHistory() {
        scope.launch {
            repository.clearHistory()
                .onSuccess { _state.update { it.copy(history = emptyList()) } }
                .onFailure { e -> Napier.e("Failed to clear history", e) }
        }
    }

    private fun loadLibrary() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAllSongs()
                .onSuccess { songs -> _state.update { it.copy(songs = songs, isLoading = false) } }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadByGenre(genre: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getSongsByGenre(genre)
                .onSuccess { songs -> _state.update { it.copy(songs = songs, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    private fun searchSongs(query: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.searchSongs(query)
                .onSuccess { songs -> _state.update { it.copy(songs = songs, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    private fun loadPlaylists() {
        scope.launch {
            repository.getAllPlaylists()
                .onSuccess { playlists -> _state.update { it.copy(playlists = playlists) } }
                .onFailure { e -> Napier.e("Failed to load playlists", e) }
        }
    }

    private fun loadFavorites() {
        scope.launch {
            repository.getFavorites()
                .onSuccess { favs -> _state.update { it.copy(favorites = favs) } }
                .onFailure { e -> Napier.e("Failed to load favorites", e) }
        }
    }

    private fun loadHistory() {
        scope.launch {
            repository.getRecentHistory()
                .onSuccess { history -> _state.update { it.copy(history = history) } }
                .onFailure { e -> Napier.e("Failed to load history", e) }
        }
    }
}
