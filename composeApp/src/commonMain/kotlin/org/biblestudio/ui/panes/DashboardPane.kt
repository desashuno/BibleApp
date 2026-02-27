package org.biblestudio.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.dashboard.component.DashboardState
import org.biblestudio.ui.theme.Spacing

/**
 * Dashboard pane: card grid showing aggregated stats from multiple features.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardPane(stateFlow: StateFlow<DashboardState>, modifier: Modifier = Modifier) {
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
    Card(
        modifier = Modifier.width(CARD_WIDTH)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16).fillMaxWidth()) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val CARD_WIDTH = Spacing.Space48 * 3
