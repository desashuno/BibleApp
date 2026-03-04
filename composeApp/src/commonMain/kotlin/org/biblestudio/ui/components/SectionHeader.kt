package org.biblestudio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.biblestudio.ui.theme.Spacing

/**
 * Primary section title used in settings, import/export, and detail views.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = Spacing.Space16, bottom = Spacing.Space8),
    )
}
