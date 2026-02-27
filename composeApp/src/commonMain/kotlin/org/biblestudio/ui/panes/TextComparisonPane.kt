package org.biblestudio.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.component.ComparisonViewMode
import org.biblestudio.features.bible_reader.component.DiffSegment
import org.biblestudio.features.bible_reader.component.DiffType
import org.biblestudio.features.bible_reader.component.TextComparisonState
import org.biblestudio.ui.theme.Spacing

/**
 * Text Comparison pane: side-by-side or interleaved diff view of multiple translations.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun TextComparisonPane(
    stateFlow: StateFlow<TextComparisonState>,
    onViewModeChanged: (ComparisonViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // View mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comparison",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            SingleChoiceSegmentedButtonRow {
                ComparisonViewMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.viewMode == mode,
                        onClick = { onViewModeChanged(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ComparisonViewMode.entries.size
                        )
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
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
        } else if (state.comparison == null) {
            Text(
                text = "Select a verse to compare translations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            val versions = state.comparison!!.versions
            when (state.viewMode) {
                ComparisonViewMode.PARALLEL -> ParallelView(versions)
                ComparisonViewMode.INTERLEAVED -> InterleavedView(versions, state.diffHighlights)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ParallelView(versions: Map<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        versions.entries.forEachIndexed { index, (abbreviation, text) ->
            if (index > 0) {
                VerticalDivider(modifier = Modifier.fillMaxHeight())
            }
            Column(
                modifier = Modifier
                    .width(Spacing.Space48 * 5)
                    .padding(Spacing.Space12)
            ) {
                Text(
                    text = abbreviation,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Space8))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun InterleavedView(versions: Map<String, String>, diffHighlights: List<DiffSegment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)
    ) {
        items(versions.entries.toList()) { (abbreviation, text) ->
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(
                text = abbreviation,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.Space4))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
            HorizontalDivider()
        }

        // Diff highlights summary
        if (diffHighlights.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(Spacing.Space16))
                Text(
                    text = "Word-level Differences",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
                DiffHighlightRow(segments = diffHighlights)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun DiffHighlightRow(segments: List<DiffSegment>) {
    val annotatedText = buildAnnotatedString {
        segments.forEach { segment ->
            val style = when (segment.type) {
                DiffType.EQUAL -> SpanStyle()
                DiffType.ADDED -> SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer,
                    fontWeight = FontWeight.Bold
                )
                DiffType.REMOVED -> SpanStyle(
                    background = MaterialTheme.colorScheme.errorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            withStyle(style) { append(segment.text) }
        }
    }
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium
    )
}
