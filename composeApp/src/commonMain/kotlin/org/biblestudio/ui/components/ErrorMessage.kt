package org.biblestudio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.Spacing

/**
 * Styled error message using [MaterialTheme.colorScheme.error].
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(Spacing.Space16),
    )
}
