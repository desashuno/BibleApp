package org.biblestudio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.Spacing

/**
 * Standard pane header: title in [titleMedium] with 16 dp padding followed by a [HorizontalDivider].
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun PaneHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(Spacing.Space16)
    )
    HorizontalDivider()
}
