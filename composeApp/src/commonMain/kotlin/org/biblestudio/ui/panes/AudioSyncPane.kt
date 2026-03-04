package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.audio_sync.component.AudioPlayerState
import org.biblestudio.features.audio_sync.component.PlaybackState
import org.biblestudio.features.audio_sync.domain.entities.AudioSyncPoint
import org.biblestudio.ui.components.ErrorMessage
import org.biblestudio.ui.components.LoadingIndicator
import org.biblestudio.ui.theme.Spacing

/**
 * Audio Sync pane: playback controls, progress bar, and verse-sync list.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun AudioSyncPane(
    stateFlow: StateFlow<AudioPlayerState>,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onJumpToVerse: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(Spacing.Space16)) {
        // Track info
        if (state.track != null) {
            Text(
                text = state.track!!.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            state.track!!.narrator?.let { narrator ->
                Text(
                    text = "Narrator: $narrator",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Space16))
        }

        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        state.error?.let { err ->
            ErrorMessage(message = err)
        }

        // Playback controls
        if (state.track != null) {
            // Seekable progress slider
            Slider(
                value = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f,
                onValueChange = { ratio -> onSeek((ratio * state.durationMs).toLong()) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.Space4))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatMs(state.positionMs),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatMs(state.durationMs),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Space8))

            // Control buttons
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onStop) { Text("Stop") }
                Spacer(modifier = Modifier.width(Spacing.Space8))
                when (state.playbackState) {
                    PlaybackState.Playing -> OutlinedButton(onClick = onPause) { Text("Pause") }
                    else -> OutlinedButton(onClick = onPlay) { Text("Play") }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Space16))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.Space8))

            // Verse sync point list
            Text(
                text = "Verses (${state.syncPoints.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.syncPoints, key = { it.id }) { sp ->
                    SyncPointRow(
                        syncPoint = sp,
                        isActive = state.currentSyncPoint?.id == sp.id,
                        onClick = { onJumpToVerse(sp.globalVerseId) }
                    )
                }
            }
        } else if (!state.isLoading) {
            Text(
                text = "Select a verse to load audio",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SyncPointRow(syncPoint: AudioSyncPoint, isActive: Boolean, onClick: () -> Unit) {
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
            Text(
                text = "Verse ${syncPoint.globalVerseId}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatMs(syncPoint.startMs)} – ${formatMs(syncPoint.endMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Suppress("MagicNumber")
private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
