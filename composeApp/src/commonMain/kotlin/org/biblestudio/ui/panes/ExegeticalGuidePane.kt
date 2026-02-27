package org.biblestudio.ui.panes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.exegetical_guide.component.ExegeticalGuideState
import org.biblestudio.ui.theme.Spacing

/**
 * Exegetical Guide pane: commentaries, cross-references, and key words for a verse.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun ExegeticalGuidePane(stateFlow: StateFlow<ExegeticalGuideState>, modifier: Modifier = Modifier) {
    val state by stateFlow.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.Space16)
    ) {
        item {
            Text(
                text = "Exegetical Guide",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )
        }

        if (state.globalVerseId == null) {
            item {
                Text(
                    text = "Select a verse to view the exegetical guide",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@LazyColumn
        }

        item {
            Text(
                text = "Verse ${state.globalVerseId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.Space16)
            )
        }

        // ── Commentaries ──
        item {
            SectionHeader("Commentaries (${state.commentaries.size})")
        }
        if (state.commentaries.isEmpty()) {
            item {
                Text(
                    text = "No commentaries available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
            }
        } else {
            items(state.commentaries, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)
                ) {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Spacing.Space8)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.Space16)) }

        // ── Cross-References ──
        item {
            SectionHeader("Cross-References (${state.crossReferences.size})")
        }
        if (state.crossReferences.isEmpty()) {
            item {
                Text(
                    text = "No cross-references",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
            }
        } else {
            items(state.crossReferences, key = { it.id }) { ref ->
                Text(
                    text = "→ Verse ${ref.targetVerseId}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = Spacing.Space2)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.Space16)) }

        // ── Key Words ──
        item {
            SectionHeader("Key Words (${state.keyWords.size})")
        }
        if (state.keyWords.isEmpty()) {
            item {
                Text(
                    text = "No key words",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
            }
        } else {
            items(state.keyWords, key = { it.strongsNumber }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)
                ) {
                    Column(modifier = Modifier.padding(Spacing.Space8)) {
                        Text(
                            text = "${entry.originalWord} (${entry.transliteration})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entry.definition,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Spacing.Space4)
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Spacing.Space8))
    }
}
