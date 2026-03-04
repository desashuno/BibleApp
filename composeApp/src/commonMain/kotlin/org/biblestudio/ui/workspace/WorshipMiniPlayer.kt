package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.biblestudio.features.worship.PlaybackStatus
import org.biblestudio.features.worship.WorshipPlayer
import org.biblestudio.ui.theme.IconSize
import org.biblestudio.ui.theme.Spacing

private val MINI_PLAYER_HEIGHT = 32.dp
private val MINI_ICON = IconSize.Small
private val MINI_BTN = IconSize.Medium
private val PROGRESS_HEIGHT = 2.dp

/**
 * Compact mini-player strip for the desktop status bar.
 * Shows song title, prev/play-pause/next controls, and a thin progress bar.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WorshipMiniPlayer(player: WorshipPlayer, modifier: Modifier = Modifier) {
    val state by player.state.collectAsState()
    val song = state.currentSong ?: return

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.height(MINI_PLAYER_HEIGHT)
        ) {
            // Title + artist
            Text(
                text = "${song.title} — ${song.artist}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(Modifier.width(Spacing.Space8))

            // Previous
            IconButton(
                onClick = player::skipPrevious,
                modifier = Modifier.size(MINI_BTN)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(MINI_ICON)
                )
            }

            // Play/Pause
            IconButton(
                onClick = {
                    when (state.playbackState) {
                        PlaybackStatus.Playing -> player.pause()
                        else -> player.resume()
                    }
                },
                modifier = Modifier.size(MINI_BTN)
            ) {
                Icon(
                    imageVector = if (state.playbackState == PlaybackStatus.Playing) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(MINI_ICON)
                )
            }

            // Next
            IconButton(
                onClick = player::skipNext,
                modifier = Modifier.size(MINI_BTN)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(MINI_ICON)
                )
            }
        }

        // Thin progress bar
        val progress = if (state.durationMs > 0) {
            state.positionMs.toFloat() / state.durationMs.toFloat()
        } else {
            0f
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(PROGRESS_HEIGHT),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
        )
    }
}

private val MOBILE_MINI_HEIGHT = 56.dp

/**
 * Mobile mini-player bar (taller, above BottomNavBar).
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WorshipMobileMiniPlayer(player: WorshipPlayer, modifier: Modifier = Modifier) {
    val state by player.state.collectAsState()
    val song = state.currentSong ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        // Thin progress bar at top
        val progress = if (state.durationMs > 0) {
            state.positionMs.toFloat() / state.durationMs.toFloat()
        } else {
            0f
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(PROGRESS_HEIGHT),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(MOBILE_MINI_HEIGHT)
                .padding(horizontal = Spacing.Space12)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = player::skipPrevious) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            IconButton(
                onClick = {
                    when (state.playbackState) {
                        PlaybackStatus.Playing -> player.pause()
                        else -> player.resume()
                    }
                }
            ) {
                Icon(
                    imageVector = if (state.playbackState == PlaybackStatus.Playing) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = "Play/Pause"
                )
            }
            IconButton(onClick = player::skipNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}
