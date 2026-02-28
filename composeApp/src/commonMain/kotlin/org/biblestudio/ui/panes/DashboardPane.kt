package org.biblestudio.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.dashboard.component.DashboardState
import org.biblestudio.ui.theme.Spacing

/**
 * Dashboard pane: landing page with Daily Verse, Continue Reading, stats,
 * Reading Plan progress, Recent Notes, and Recent History widgets.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardPane(
    stateFlow: StateFlow<DashboardState>,
    onContinueReading: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.Space16)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = Spacing.Space16)
            )
        }

        // ── Daily Verse ──
        state.dailyVerse?.let { dv ->
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Space16)) {
                    Column(modifier = Modifier.padding(Spacing.Space16)) {
                        Text(
                            text = "Daily Verse",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.Space8))
                        Text(
                            text = "\"${dv.text}\"",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(Spacing.Space4))
                        Text(
                            text = "— ${dv.reference}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Continue Reading ──
        state.continueReading?.let { cr ->
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Space16)) {
                    Row(
                        modifier = Modifier.padding(Spacing.Space16).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue Reading",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cr.reference,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        OutlinedButton(onClick = { onContinueReading(cr.globalVerseId) }) {
                            Text("Resume")
                        }
                    }
                }
            }
        }

        // ── Stats Grid ──
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Space16),
                verticalArrangement = Arrangement.spacedBy(Spacing.Space16)
            ) {
                StatCard(label = "Notes", count = state.totalNotes)
                StatCard(label = "Highlights", count = state.totalHighlights)
                StatCard(label = "Bookmarks", count = state.totalBookmarks)
                StatCard(label = "Sermons", count = state.totalSermons)
                StatCard(label = "Reading Plans", count = state.activePlans)
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.Space24)) }

        // ── Reading Plan Progress ──
        state.readingPlanProgress?.let { rp ->
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Space16)) {
                    Column(modifier = Modifier.padding(Spacing.Space16)) {
                        Text(
                            text = "Reading Plan Progress",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.Space8))
                        Text(
                            text = rp.planTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.Space4))
                        LinearProgressIndicator(
                            progress = { rp.progressPercent },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Spacing.Space4))
                        Text(
                            text = "Day ${rp.currentDay} of ${rp.totalDays}  •  Streak: ${rp.streak}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Recent Notes ──
        if (state.recentNotes.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Notes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
            }
            items(state.recentNotes, key = { it.uuid }) { note ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)) {
                    Column(modifier = Modifier.padding(Spacing.Space8)) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Recent History ──
        item {
            Text(
                text = "Recent History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )
        }

        if (state.recentHistory.isEmpty()) {
            item {
                Text(
                    text = "No recent history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.recentHistory) { entry ->
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = Spacing.Space2)
                )
            }
        }

        state.error?.let { err ->
            item {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Spacing.Space8)
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun StatCard(label: String, count: Int) {
    Card(modifier = Modifier.width(Spacing.Space48 * 3)) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
