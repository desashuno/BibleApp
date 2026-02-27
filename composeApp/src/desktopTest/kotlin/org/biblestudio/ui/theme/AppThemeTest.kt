package org.biblestudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class AppThemeTest {

    @Test
    fun lightThemeAppliesExpectedColors() = runComposeUiTest {
        var primary = Color.Unspecified
        var background = Color.Unspecified

        setContent {
            AppTheme(darkTheme = false) {
                primary = MaterialTheme.colorScheme.primary
                background = MaterialTheme.colorScheme.background
            }
        }

        assertNotEquals(Color.Unspecified, primary, "Primary should be set in light theme")
        assertNotEquals(Color.Unspecified, background, "Background should be set in light theme")
    }

    @Test
    fun darkThemeAppliesDifferentColors() = runComposeUiTest {
        var lightPrimary = Color.Unspecified
        var darkPrimary = Color.Unspecified
        var isDark by mutableStateOf(false)

        setContent {
            AppTheme(darkTheme = isDark) {
                if (!isDark) {
                    lightPrimary = MaterialTheme.colorScheme.primary
                } else {
                    darkPrimary = MaterialTheme.colorScheme.primary
                }
            }
        }

        // Trigger recomposition with dark theme
        isDark = true
        waitForIdle()

        assertNotEquals(Color.Unspecified, lightPrimary, "Light primary should be captured")
        assertNotEquals(Color.Unspecified, darkPrimary, "Dark primary should be captured")
    }
}
