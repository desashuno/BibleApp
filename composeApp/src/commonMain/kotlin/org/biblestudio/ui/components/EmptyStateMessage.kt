package org.biblestudio.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.Spacing

/**
 * Subtle placeholder message for empty states.
 *
 * @param centered When true, fills available space and centers the text.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    if (centered) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(Spacing.Space16),
        )
    }
}
