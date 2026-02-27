package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.cross_references.component.CrossReferenceState
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.ui.theme.Spacing

/**
 * Cross-Reference pane: shows references for the selected verse with type badges.
 * Supports inline expansion to reveal target verse text.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CrossReferencePane(
    stateFlow: StateFlow<CrossReferenceState>,
    onReferenceTapped: (CrossReference) -> Unit,
    onToggleExpansion: (CrossReference) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = if (state.sourceVerseId != null) {
                "Cross-References for ${state.sourceVerseId}"
            } else {
                "Cross-References"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(Spacing.Space16)
        )
        HorizontalDivider()

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else if (state.references.isEmpty()) {
            Text(
                text = if (state.sourceVerseId != null) {
                    "No cross-references found."
                } else {
                    "Select a verse to see cross-references."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.references, key = { it.id }) { ref ->
                    val expandedText = state.expandedVerseTexts[ref.targetVerseId]
                    CrossReferenceRow(
                        ref = ref,
                        expandedText = expandedText,
                        onClick = { onReferenceTapped(ref) },
                        onToggleExpansion = { onToggleExpansion(ref) }
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun CrossReferenceRow(
    ref: CrossReference,
    expandedText: String?,
    onClick: () -> Unit,
    onToggleExpansion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type badge
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(Spacing.Space4)
            ) {
                Text(
                    text = ref.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Space8,
                        vertical = Spacing.Space2
                    )
                )
            }

            Spacer(modifier = Modifier.width(Spacing.Space12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Verse ${ref.targetVerseId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                @Suppress("MagicNumber")
                Text(
                    text = "Confidence: ${"%.0f".format(ref.confidence * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expand / Collapse toggle
            Text(
                text = if (expandedText != null) "▲" else "▼",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onToggleExpansion)
            )
        }

        // Inline expansion: show target verse text
        if (expandedText != null) {
            Text(
                text = expandedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = Spacing.Space48,
                    top = Spacing.Space4,
                    end = Spacing.Space16
                )
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}
