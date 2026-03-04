package org.biblestudio.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.word_study.component.WordStudyState
import org.biblestudio.core.study.LexiconEntry
import org.biblestudio.ui.components.LoadingErrorContent
import org.biblestudio.ui.components.PaneHeader
import org.biblestudio.ui.theme.Spacing

/**
 * Word Study pane: definition card, occurrence frequency chart, and occurrence list.
 *
 * Displays lexicon data for a Strong's number received via VerseBus.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun WordStudyPane(
    stateFlow: StateFlow<WordStudyState>,
    onOccurrenceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        PaneHeader(
            title = state.entry?.let { "${it.strongsNumber} — ${it.originalWord}" } ?: "Word Study"
        )

        LoadingErrorContent(
            isLoading = state.isLoading,
            error = state.error,
            data = state.entry,
            emptyMessage = "Select a word to study",
            modifier = Modifier.fillMaxSize(),
        ) { entry ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)) {
                // Definition card
                item {
                    LexiconCard(
                        entry = entry,
                        occurrenceCount = state.occurrenceCount
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space16))
                }

                // Frequency chart
                if (state.occurrences.isNotEmpty()) {
                    item {
                        Text(
                            text = "Frequency by Book",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = Spacing.Space8)
                        )
                        FrequencyChart(occurrences = state.occurrences)
                        Spacer(modifier = Modifier.height(Spacing.Space16))
                    }
                }

                // Occurrence list header
                item {
                    Text(
                        text = "Occurrences",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = Spacing.Space8)
                    )
                }
                items(state.occurrences, key = { it.id }) { occ ->
                    OccurrenceRow(
                        occurrence = occ,
                        onClick = { onOccurrenceSelected(occ.globalVerseId.toInt()) }
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LexiconCard(entry: LexiconEntry, occurrenceCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Text(
                text = entry.originalWord,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "(${entry.transliteration})",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(
                text = "Definition: ${entry.definition}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (entry.usageNotes != null) {
                Spacer(modifier = Modifier.height(Spacing.Space4))
                Text(
                    text = entry.usageNotes.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(
                text = "Usage: $occurrenceCount occurrences",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Simple horizontal bar chart showing occurrence counts grouped by book number.
 * Book number is derived from globalVerseId / 1_000_000.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun FrequencyChart(occurrences: List<WordOccurrence>) {
    val bookCounts = occurrences
        .groupBy { it.globalVerseId / 1_000_000 }
        .mapValues { it.value.size }
        .toSortedMap()

    val maxCount = bookCounts.values.maxOrNull() ?: 1

    Column(modifier = Modifier.fillMaxWidth()) {
        bookCounts.forEach { (bookNum, count) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Book $bookNum",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = Spacing.Space8)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Spacing.Space16)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = count.toFloat() / maxCount)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = Spacing.Space4)
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun OccurrenceRow(occurrence: WordOccurrence, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.Space4)
    ) {
        Text(
            text = "${VerseRefFormatter.formatShort(occurrence.globalVerseId)} (position ${occurrence.wordPosition})",
            style = MaterialTheme.typography.bodyMedium
        )
    }
    HorizontalDivider()
}
