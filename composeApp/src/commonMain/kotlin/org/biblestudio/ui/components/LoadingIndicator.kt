package org.biblestudio.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.Spacing

/**
 * Centered circular progress indicator.
 *
 * @param fullScreen When true, the indicator fills all available space.
 *   When false, it adds standard padding and centers horizontally.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier, fullScreen: Boolean = false) {
    if (fullScreen) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier.padding(Spacing.Space24),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
