package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.worship.PlaybackStatus
import org.biblestudio.features.worship.WorshipPlayer
import org.biblestudio.features.worship.WorshipPlayerState
import org.biblestudio.features.worship.component.WorshipState
import org.biblestudio.features.worship.component.WorshipTab
import org.biblestudio.features.worship.domain.entities.RepeatMode
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.ui.components.ErrorMessage
import org.biblestudio.ui.components.LoadingIndicator
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.workspace.LocalWorshipPlayer

/**
 * Full worship pane with tabs (Library, Playlists, Favorites, Queue, History)
 * and a player section at the bottom.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun WorshipPane(
    stateFlow: StateFlow<WorshipState>,
    onTabSelected: (WorshipTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSongSelected: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val player = LocalWorshipPlayer.current

    Column(modifier = modifier.fillMaxSize()) {
        // Tabs
        val tabs = WorshipTab.entries
        TabRow(selectedTabIndex = tabs.indexOf(state.activeTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.activeTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.name, maxLines = 1) }
                )
            }
        }

        // Tab content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.Space12, vertical = Spacing.Space8)
        ) {
            when (state.activeTab) {
                WorshipTab.Library -> LibraryTab(
                    songs = state.songs,
                    searchQuery = state.searchQuery,
                    isLoading = state.isLoading,
                    error = state.error,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onSongSelected = onSongSelected,
                    onPlayAll = onPlayAll,
                    onToggleFavorite = onToggleFavorite
                )
                WorshipTab.Playlists -> PlaylistsTab(
                    playlists = state.playlists,
                    onCreatePlaylist = onCreatePlaylist,
                    onDeletePlaylist = onDeletePlaylist
                )
                WorshipTab.Favorites -> SongList(
                    songs = state.favorites,
                    onSongSelected = onSongSelected,
                    onToggleFavorite = onToggleFavorite,
                    isFavoriteList = true
                )
                WorshipTab.Queue -> QueueTab(player = player)
                WorshipTab.History -> HistoryTab(
                    songs = state.history,
                    onSongSelected = onSongSelected,
                    onClearHistory = onClearHistory
                )
            }
        }

        // Player section at bottom
        if (player != null) {
            HorizontalDivider()
            PlayerSection(player = player)
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun LibraryTab(
    songs: List<Song>,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchQueryChanged: (String) -> Unit,
    onSongSelected: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    Column {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search songs...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.Space8))

        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onPlayAll) { Text("Play All") }
            }
            Spacer(Modifier.height(Spacing.Space8))
        }

        if (isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        error?.let { ErrorMessage(message = it) }

        SongList(
            songs = songs,
            onSongSelected = onSongSelected,
            onToggleFavorite = onToggleFavorite
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SongList(
    songs: List<Song>,
    onSongSelected: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    isFavoriteList: Boolean = false
) {
    LazyColumn {
        items(songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                isFavorite = isFavoriteList,
                onClick = { onSongSelected(song) },
                onToggleFavorite = { onToggleFavorite(song.id) }
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SongRow(
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space2)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Space12, vertical = Spacing.Space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Spacing.Space8))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} — ${song.album}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PlaylistsTab(
    playlists: List<org.biblestudio.features.worship.domain.entities.Playlist>,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    var newPlaylistName by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newPlaylistName,
                onValueChange = { newPlaylistName = it },
                placeholder = { Text("New playlist name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(Spacing.Space8))
            OutlinedButton(
                onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        onCreatePlaylist(newPlaylistName)
                        newPlaylistName = ""
                    }
                }
            ) { Text("Create") }
        }
        Spacer(Modifier.height(Spacing.Space8))

        LazyColumn {
            items(playlists, key = { it.id }) { playlist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Space2)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.Space12, vertical = Spacing.Space8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${playlist.songs.size} songs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun QueueTab(player: WorshipPlayer?) {
    if (player == null) {
        Text(
            text = "No player available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val pState by player.state.collectAsState()
    if (pState.queue.isEmpty()) {
        Text(
            text = "Queue is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.Space16)
        )
    } else {
        LazyColumn {
            items(pState.queue, key = { it.id }) { song ->
                val isPlaying = pState.currentSong?.id == song.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Space2)
                        .clickable { player.play(song, pState.queue) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.Space12, vertical = Spacing.Space8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun HistoryTab(
    songs: List<Song>,
    onSongSelected: (Song) -> Unit,
    onClearHistory: () -> Unit
) {
    Column {
        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onClearHistory) { Text("Clear History") }
            }
            Spacer(Modifier.height(Spacing.Space8))
        }
        SongList(
            songs = songs,
            onSongSelected = onSongSelected,
            onToggleFavorite = {}
        )
        if (songs.isEmpty()) {
            Text(
                text = "No listening history yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun PlayerSection(player: WorshipPlayer) {
    val pState by player.state.collectAsState()
    val song = pState.currentSong

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Space12)
    ) {
        if (song != null) {
            // Song info
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            // Current lyric line
            pState.currentLyricLine?.let { line ->
                Spacer(Modifier.height(Spacing.Space4))
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(Spacing.Space8))

            // Seek slider
            Slider(
                value = if (pState.durationMs > 0) {
                    pState.positionMs.toFloat() / pState.durationMs.toFloat()
                } else {
                    0f
                },
                onValueChange = { ratio -> player.seekTo((ratio * pState.durationMs).toLong()) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(formatDuration(pState.positionMs), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(pState.durationMs), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(Spacing.Space4))

            // Transport controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = player::toggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (pState.isShuffleOn) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                // Previous
                IconButton(onClick = player::skipPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                // Play/Pause
                IconButton(onClick = {
                    when (pState.playbackState) {
                        PlaybackStatus.Playing -> player.pause()
                        else -> player.resume()
                    }
                }) {
                    Icon(
                        imageVector = if (pState.playbackState == PlaybackStatus.Playing) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = "Play/Pause"
                    )
                }
                // Next
                IconButton(onClick = player::skipNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
                // Repeat
                IconButton(onClick = player::toggleRepeatMode) {
                    Icon(
                        imageVector = when (pState.repeatMode) {
                            RepeatMode.One -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (pState.repeatMode != RepeatMode.Off) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        } else {
            Text(
                text = "Select a song to start playing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Suppress("MagicNumber")
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
