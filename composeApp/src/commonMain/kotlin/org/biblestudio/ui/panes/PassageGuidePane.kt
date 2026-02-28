package org.biblestudio.ui.panes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.passage_guide.component.PassageGuideState
import org.biblestudio.ui.theme.Spacing

/**
 * Passage Guide pane: sectioned card layout aggregating cross-references,
 * outlines, key words, commentary, and notes for a verse.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun PassageGuidePane(
    stateFlow: StateFlow<PassageGuideState>,
    onRefSelected: (CrossReference) -> Unit,
    onWordSelected: (String) -> Unit,
    onSectionToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Passage Guide",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(Spacing.Space16)
        )

        HorizontalDivider()

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
            state.report == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Select a verse to see the passage guide")
                }
            }
            else -> {
                val report = state.report!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(Spacing.Space16)
                ) {
                    // Verse text header
                    item {
                        if (report.verseText.isNotEmpty()) {
                            Text(
                                text = report.verseText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = Spacing.Space16)
                            )
                        }
                    }

                    // Cross-References section
                    item {
                        GuideSection(
                            title = "Cross-References",
                            count = report.crossReferences.size,
                            sectionId = "crossRefs",
                            expanded = state.expandedSections.contains("crossRefs"),
                            onToggle = onSectionToggle
                        ) {
                            report.crossReferences.forEach { ref ->
                                Text(
                                    text = "${VerseRefFormatter.format(ref.targetVerseId)} (${ref.type})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onRefSelected(ref) }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Outlines section
                    item {
                        GuideSection(
                            title = "Outlines",
                            count = report.outlines.size,
                            sectionId = "outlines",
                            expanded = state.expandedSections.contains("outlines"),
                            onToggle = onSectionToggle
                        ) {
                            report.outlines.forEach { outline ->
                                Text(
                                    text = outline.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                                if (outline.content.isNotEmpty()) {
                                    Text(
                                        text = outline.content,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Key Words section
                    item {
                        GuideSection(
                            title = "Key Words",
                            count = report.keyWords.size,
                            sectionId = "keyWords",
                            expanded = state.expandedSections.contains("keyWords"),
                            onToggle = onSectionToggle
                        ) {
                            report.keyWords.forEach { entry ->
                                Text(
                                    text = "${entry.strongsNumber} — ${entry.originalWord}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onWordSelected(entry.strongsNumber) }
                                        .padding(vertical = 2.dp)
                                )
                                Text(
                                    text = entry.definition,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Commentary section
                    item {
                        GuideSection(
                            title = "Commentary",
                            count = report.commentaryEntries.size,
                            sectionId = "commentary",
                            expanded = state.expandedSections.contains("commentary"),
                            onToggle = onSectionToggle
                        ) {
                            report.commentaryEntries.forEach { entry ->
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Notes section
                    item {
                        GuideSection(
                            title = "Notes",
                            count = report.userNotes.size,
                            sectionId = "notes",
                            expanded = state.expandedSections.contains("notes"),
                            onToggle = onSectionToggle
                        ) {
                            report.userNotes.forEach { note ->
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A collapsible section with header (title + count badge) and content.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun GuideSection(
    title: String,
    count: Int,
    sectionId: String,
    expanded: Boolean,
    onToggle: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space8)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(sectionId) }
                    .padding(Spacing.Space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "▼" else "▶",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(Spacing.Space8))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(Spacing.Space8))
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = Spacing.Space16,
                        top = Spacing.Space8
                    )
                ) {
                    content()
                }
            }
        }
    }
}
