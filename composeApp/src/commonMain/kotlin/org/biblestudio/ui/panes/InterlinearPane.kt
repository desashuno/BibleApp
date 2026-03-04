package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.morphology_interlinear.component.InterlinearDisplayMode
import org.biblestudio.features.morphology_interlinear.component.InterlinearState
import org.biblestudio.core.study.MorphWord
import org.biblestudio.ui.components.LoadingErrorContent
import org.biblestudio.ui.components.PaneHeader
import org.biblestudio.ui.theme.Spacing

/**
 * Interlinear pane: displays a word grid with original text,
 * transliteration, gloss, and parsing rows for each word in a verse.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterlinearPane(
    stateFlow: StateFlow<InterlinearState>,
    onWordSelected: (MorphWord) -> Unit,
    onDisplayModeChanged: (InterlinearDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        PaneHeader(title = "Interlinear")

        // Display mode chip row
        DisplayModeSelector(
            currentMode = state.displayMode,
            onModeChanged = onDisplayModeChanged,
            modifier = Modifier.padding(horizontal = Spacing.Space16)
        )

        LoadingErrorContent(
            isLoading = state.isLoading,
            error = state.error,
            data = state.words,
            emptyMessage = "Select a verse to view interlinear",
            modifier = Modifier.fillMaxSize(),
            isEmpty = { it.isEmpty() },
        ) { words ->
            WordGrid(
                words = words,
                decodedParsings = state.decodedParsings,
                displayMode = state.displayMode,
                onWordSelected = onWordSelected,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.Space16)
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DisplayModeSelector(
    currentMode: InterlinearDisplayMode,
    onModeChanged: (InterlinearDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    @Suppress("ktlint:standard:max-line-length")
    val modes = InterlinearDisplayMode.entries
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space8)
    ) {
        modes.forEach { mode ->
            FilterChip(
                selected = mode == currentMode,
                onClick = { onModeChanged(mode) },
                label = { Text(text = mode.name) }
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordGrid(
    words: List<MorphWord>,
    decodedParsings: Map<String, String>,
    displayMode: InterlinearDisplayMode,
    onWordSelected: (MorphWord) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space16),
        verticalArrangement = Arrangement.spacedBy(Spacing.Space8)
    ) {
        words.forEach { word ->
            WordCell(
                word = word,
                decodedParsing = decodedParsings[word.parsingCode].orEmpty(),
                displayMode = displayMode,
                onClick = { onWordSelected(word) }
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun WordCell(
    word: MorphWord,
    decodedParsing: String,
    displayMode: InterlinearDisplayMode,
    onClick: () -> Unit
) {
    // Determine which rows to show based on display mode
    val showTransliteration = displayMode != InterlinearDisplayMode.Inline
    val showGloss = true
    val showParsing = displayMode == InterlinearDisplayMode.Interlinear

    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Space8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Original-language surface form
            Text(
                text = word.surfaceForm,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            // Transliteration (lemma) — hidden in Inline mode
            if (showTransliteration) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = word.lemma,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Gloss — always shown
            if (showGloss) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = word.gloss,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Parsing badge — only shown in Interlinear mode
            if (showParsing && decodedParsing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = decodedParsing,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
