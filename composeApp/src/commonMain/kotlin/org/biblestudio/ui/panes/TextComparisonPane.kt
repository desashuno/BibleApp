package org.biblestudio.ui.panes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.bible_reader.component.ComparisonViewMode
import org.biblestudio.features.bible_reader.component.DiffSegment
import org.biblestudio.features.bible_reader.component.wordDiff
import org.biblestudio.features.bible_reader.component.DiffType
import org.biblestudio.features.bible_reader.component.TextComparisonState
import org.biblestudio.features.bible_reader.domain.entities.VersionVerse
import org.biblestudio.ui.theme.LocalRedLetter
import org.biblestudio.ui.components.EmptyStateMessage
import org.biblestudio.ui.components.ErrorMessage
import org.biblestudio.ui.components.LoadingIndicator
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.util.extractRedLetterRanges

/**
 * Text Comparison pane: side-by-side or interleaved diff view of multiple translations.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun TextComparisonPane(
    stateFlow: StateFlow<TextComparisonState>,
    onViewModeChanged: (ComparisonViewMode) -> Unit,
    onSelectedVersionsChanged: (List<String>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    var showVersionSelector by remember { mutableStateOf(false) }

    if (showVersionSelector) {
        VersionSelectorDialog(
            availableVersions = state.availableBibles.map { it.abbreviation },
            selectedVersions = state.selectedVersions,
            onConfirm = { selected ->
                onSelectedVersionsChanged(selected)
                showVersionSelector = false
            },
            onDismiss = { showVersionSelector = false }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // View mode toggle + version selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Comparison",
                    style = MaterialTheme.typography.titleMedium
                )
                state.comparison?.let { cmp ->
                    Text(
                        text = VerseRefFormatter.format(cmp.globalVerseId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            OutlinedButton(onClick = { showVersionSelector = true }) {
                Text("Versions (${state.selectedVersions.size})")
            }
            Spacer(modifier = Modifier.width(Spacing.Space8))
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
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (state.error != null) {
            ErrorMessage(message = state.error ?: "Error")
        } else if (state.comparison == null) {
            EmptyStateMessage(message = "Select a verse to compare translations.")
        } else {
            // Filter versions by selection
            val allVersions = state.comparison!!.versions
            val filtered = if (state.selectedVersions.isEmpty()) {
                allVersions
            } else {
                allVersions.filterKeys { it in state.selectedVersions }
            }
            when (state.viewMode) {
                ComparisonViewMode.PARALLEL -> ParallelView(filtered)
                ComparisonViewMode.INTERLEAVED -> InterleavedView(filtered, state.diffHighlights)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun VersionSelectorDialog(
    availableVersions: List<String>,
    selectedVersions: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var checked by remember(selectedVersions) { mutableStateOf(selectedVersions.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Versions") },
        text = {
            LazyColumn {
                items(availableVersions) { version ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = version in checked,
                            onCheckedChange = { isChecked ->
                                checked = if (isChecked) checked + version else checked - version
                            }
                        )
                        Text(
                            text = version,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(checked.toList()) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun ParallelView(versions: Map<String, VersionVerse>) {
    val baseline = versions.values.firstOrNull()?.text
    val baselineWords = baseline?.split(" ").orEmpty()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val versionCount = versions.size.coerceAtLeast(1)
        val columnWidth = (maxWidth / versionCount).coerceAtLeast(150.dp)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            versions.entries.forEachIndexed { index, (abbreviation, payload) ->
                if (index > 0) {
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                }
                Column(
                    modifier = Modifier
                        .widthIn(min = 150.dp)
                        .width(columnWidth)
                        .padding(Spacing.Space12)
                ) {
                    Text(
                        text = abbreviation,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Diff vs baseline (first version)
                    if (index > 0 && baseline != null) {
                        val diff = wordDiff(
                            baselineWords,
                            payload.text.split(" ")
                        )
                        DiffHighlightRow(segments = diff)
                    } else {
                        RedLetterText(payload = payload, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun InterleavedView(versions: Map<String, VersionVerse>, diffHighlights: List<DiffSegment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)
    ) {
        items(versions.entries.toList()) { (abbreviation, payload) ->
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(
                text = abbreviation,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.Space4))
            RedLetterText(payload = payload, style = MaterialTheme.typography.bodyMedium)
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
        segments.forEachIndexed { index, segment ->
            if (index > 0) append(" ")
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

@Suppress("ktlint:standard:function-naming")
@Composable
private fun RedLetterText(payload: VersionVerse, style: TextStyle) {
    val redLetter = LocalRedLetter.current
    val redColor = MaterialTheme.colorScheme.error
    val redRanges = if (redLetter) {
        extractRedLetterRanges(payload.htmlText, payload.text)
    } else {
        emptyList()
    }

    val annotated = buildAnnotatedString {
        if (redRanges.isEmpty()) {
            append(payload.text)
            return@buildAnnotatedString
        }

        var index = 0
        while (index < payload.text.length) {
            val inRed = redRanges.any { index in it }
            var end = index + 1
            while (end < payload.text.length && redRanges.any { end in it } == inRed) {
                end++
            }

            val chunk = payload.text.substring(index, end)
            if (inRed) {
                withStyle(SpanStyle(color = redColor)) { append(chunk) }
            } else {
                append(chunk)
            }

            index = end
        }
    }

    Text(
        text = annotated,
        style = style
    )
}
