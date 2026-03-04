package org.biblestudio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.biblestudio.ui.theme.Spacing

/**
 * Compact pill badge with configurable background and text colours.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun Badge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = color,
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(Spacing.Space4),
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = Spacing.Space8,
                vertical = Spacing.Space2,
            ),
        )
    }
}

/**
 * Badge that maps a semantic status colour to a pill.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun StatusBadge(
    text: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    Badge(text = text, color = statusColor, textColor = statusColor, modifier = modifier)
}
