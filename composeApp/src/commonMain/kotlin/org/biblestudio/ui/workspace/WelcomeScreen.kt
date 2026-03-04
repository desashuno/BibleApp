package org.biblestudio.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.biblestudio.core.pane_registry.PaneType
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val CARD_WIDTH = 150.dp
private val CARD_ICON_SIZE = 28.dp

private val QUICK_PANES = listOf(PaneType.BIBLE_READER, PaneType.DASHBOARD, PaneType.SEARCH, PaneType.NOTE_EDITOR)

/**
 * Welcome screen shown when the workspace has no open panes.
 *
 * Displays the BibleStudio title, a subtitle, and a grid of recommended
 * panels the user can click to open.
 */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun WelcomeScreen(onPaneSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to BibleStudio",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.Space8))
            Text(
                text = "Open a panel to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.Space24))

            // Quick-access panel cards
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.Space16))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Space12),
                verticalArrangement = Arrangement.spacedBy(Spacing.Space12)
            ) {
                QUICK_PANES.forEach { paneType ->
                    val info = remember(paneType) { PaneStyling.paneInfo(paneType) }
                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(CARD_WIDTH)
                            .clickable { onPaneSelected(paneType) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Spacing.Space16)
                        ) {
                            Icon(
                                imageVector = info.icon,
                                contentDescription = info.displayName,
                                tint = info.accentColor,
                                modifier = Modifier.size(CARD_ICON_SIZE)
                            )
                            Spacer(Modifier.height(Spacing.Space8))
                            Text(
                                text = info.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
