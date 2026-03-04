package org.biblestudio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.biblestudio.ui.theme.Spacing

/**
 * Clickable section header with expand/collapse triangle indicator.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(
            text = "${if (expanded) "▼" else "▶"} $title",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Spacing.Space4),
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Spacing.Space8))
    }
}
