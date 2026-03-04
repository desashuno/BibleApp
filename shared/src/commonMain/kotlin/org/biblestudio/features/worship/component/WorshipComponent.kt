package org.biblestudio.features.worship.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.worship.domain.entities.Playlist
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.features.worship.domain.entities.WorshipCategory

enum class WorshipTab {
    Library,
    Playlists,
    Favorites,
    Queue,
    History
}

data class WorshipState(
    val activeTab: WorshipTab = WorshipTab.Library,
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val history: List<Song> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: WorshipCategory = WorshipCategory.All,
    val selectedPlaylist: Playlist? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

interface WorshipComponent {
    val state: StateFlow<WorshipState>

    fun onTabSelected(tab: WorshipTab)
    fun onSearchQueryChanged(query: String)
    fun onCategorySelected(category: WorshipCategory)
    fun onSongSelected(song: Song)
    fun onPlayAll()
    fun onToggleFavorite(songId: Long)

    // Playlist management
    fun onPlaylistSelected(playlist: Playlist)
    fun onCreatePlaylist(name: String)
    fun onDeletePlaylist(playlistId: String)
    fun onAddSongToPlaylist(playlistId: String, songId: Long)

    fun onClearHistory()
}
