package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
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
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.exegetical_guide.component.ExegeticalGuideState
import org.biblestudio.features.exegetical_guide.component.GuideSection
import org.biblestudio.ui.components.CollapsibleSectionHeader
import org.biblestudio.ui.theme.Spacing

/**
 * Exegetical Guide pane: 6 collapsible sections — text-critical, grammatical, lexical,
 * structural, commentaries, and cross-references.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "CyclomaticComplexMethod")
@Composable
fun ExegeticalGuidePane(
    stateFlow: StateFlow<ExegeticalGuideState>,
    onToggleSection: (GuideSection) -> Unit = {},
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
                text = VerseRefFormatter.format(state.globalVerseId!!),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.Space16)
            )
        }

        // ── Text-Critical Variants ──
        item {
            CollapsibleSectionHeader(
                title = "Text-Critical Variants (${state.textVariants.size})",
                expanded = GuideSection.TextCritical in state.expandedSections,
                onClick = { onToggleSection(GuideSection.TextCritical) }
            )
        }
        if (GuideSection.TextCritical in state.expandedSections) {
            if (state.textVariants.isEmpty()) {
                item { EmptySectionText("No textual variants recorded") }
            } else {
                items(state.textVariants, key = { it.id }) { variant ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)) {
                        Column(modifier = Modifier.padding(Spacing.Space8)) {
                            Text(
                                text = variant.manuscriptSigla,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = variant.readingText, style = MaterialTheme.typography.bodyMedium)
                            if (variant.supportingManuscripts.isNotBlank()) {
                                Text(
                                    text = "Manuscripts: ${variant.supportingManuscripts}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (variant.notes.isNotBlank()) {
                                Text(text = variant.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Grammatical Notes ──
        item {
            CollapsibleSectionHeader(
                title = "Grammatical Analysis (${state.grammaticalNotes.size})",
                expanded = GuideSection.Grammatical in state.expandedSections,
                onClick = { onToggleSection(GuideSection.Grammatical) }
            )
        }
        if (GuideSection.Grammatical in state.expandedSections) {
            if (state.grammaticalNotes.isEmpty()) {
                item { EmptySectionText("No grammatical notes") }
            } else {
                items(state.grammaticalNotes, key = { it.id }) { note ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)) {
                        Column(modifier = Modifier.padding(Spacing.Space8)) {
                            Text(
                                text = "${note.word} — ${note.partOfSpeech}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Parsing: ${note.parsing}  •  Role: ${note.syntacticRole}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (note.notes.isNotBlank()) {
                                Text(text = note.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Lexical / Key Words ──
        item {
            CollapsibleSectionHeader(
                title = "Key Words (${state.keyWords.size})",
                expanded = GuideSection.Lexical in state.expandedSections,
                onClick = { onToggleSection(GuideSection.Lexical) }
            )
        }
        if (GuideSection.Lexical in state.expandedSections) {
            if (state.keyWords.isEmpty()) {
                item { EmptySectionText("No key words") }
            } else {
                items(state.keyWords, key = { it.strongsNumber }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)) {
                        Column(modifier = Modifier.padding(Spacing.Space8)) {
                            Text(
                                text = "${entry.originalWord} (${entry.transliteration})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = entry.definition, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Structural Outline ──
        item {
            CollapsibleSectionHeader(
                title = "Structural Outline",
                expanded = GuideSection.Structural in state.expandedSections,
                onClick = { onToggleSection(GuideSection.Structural) }
            )
        }
        if (GuideSection.Structural in state.expandedSections) {
            val outline = state.structuralOutline
            if (outline == null) {
                item { EmptySectionText("No structural outline available") }
            } else {
                item {
                    Text(
                        text = "${outline.title} (${outline.passageRange})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = Spacing.Space4)
                    )
                }
                items(outline.elements) { el ->
                    val indent = Spacing.Space16 * el.depth
                    Text(
                        text = "${el.label}  ${el.description}  [${el.verseRange}]",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = indent, bottom = Spacing.Space2)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Commentaries ──
        item {
            CollapsibleSectionHeader(
                title = "Commentaries (${state.commentaries.size})",
                expanded = GuideSection.Commentaries in state.expandedSections,
                onClick = { onToggleSection(GuideSection.Commentaries) }
            )
        }
        if (GuideSection.Commentaries in state.expandedSections) {
            if (state.commentaries.isEmpty()) {
                item { EmptySectionText("No commentaries available") }
            } else {
                items(state.commentaries, key = { it.id }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4)) {
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(Spacing.Space8)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(Spacing.Space16)) }
        }

        // ── Cross-References ──
        item {
            CollapsibleSectionHeader(
                title = "Cross-References (${state.crossReferences.size})",
                expanded = GuideSection.CrossReferences in state.expandedSections,
                onClick = { onToggleSection(GuideSection.CrossReferences) }
            )
        }
        if (GuideSection.CrossReferences in state.expandedSections) {
            if (state.crossReferences.isEmpty()) {
                item { EmptySectionText("No cross-references") }
            } else {
                items(state.crossReferences, key = { it.id }) { ref ->
                    Text(
                        text = "→ ${VerseRefFormatter.format(ref.targetVerseId)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = Spacing.Space2)
                    )
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

// CollapsibleSectionHeader is imported from org.biblestudio.ui.components

@Suppress("ktlint:standard:function-naming")
@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = Spacing.Space8)
    )
}
