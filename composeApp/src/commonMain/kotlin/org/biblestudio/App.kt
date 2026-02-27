package org.biblestudio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.AppTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "${AppInfo.NAME} v${AppInfo.VERSION}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
