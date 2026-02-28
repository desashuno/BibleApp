package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.morphology_interlinear.component.AlignedToken
import org.biblestudio.features.morphology_interlinear.component.ReverseInterlinearState
import org.biblestudio.ui.theme.Spacing

/**
 * Reverse Interlinear pane: English text with underlined linked words.
 *
 * Tokens aligned to original-language morphology are underlined.
 * Tapping an underlined word shows a popover with original word,
 * transliteration, parsing, and definition.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReverseInterlinearPane(
    stateFlow: StateFlow<ReverseInterlinearState>,
    onTokenSelected: (AlignedToken) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Reverse Interlinear",
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
            state.alignedTokens.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Select a verse to view reverse interlinear")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.Space16)
                ) {
                    // English text as flow of clickable tokens
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.alignedTokens.forEach { token ->
                            val isLinked = token.morphWord != null
                            val annotated = buildAnnotatedString {
                                if (isLinked) {
                                    withStyle(
                                        SpanStyle(
                                            textDecoration = TextDecoration.Underline,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        append(token.englishToken)
                                    }
                                } else {
                                    append(token.englishToken)
                                }
                            }
                            Text(
                                text = annotated,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = if (isLinked) {
                                    Modifier.clickable { onTokenSelected(token) }
                                } else {
                                    Modifier
                                }
                            )
                        }
                    }

                    // Popover card for selected token
                    state.selectedToken?.morphWord?.let { morphWord ->
                        Spacer(modifier = Modifier.height(Spacing.Space16))
                        OriginalWordPopover(
                            token = state.selectedToken!!,
                            onDismiss = onClearSelection
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun OriginalWordPopover(token: AlignedToken, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val morph = token.morphWord ?: return
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = morph.surfaceForm,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = morph.lemma,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = morph.gloss,
                style = MaterialTheme.typography.bodyMedium
            )
            token.decodedParsing?.let { parsing ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = parsing,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
