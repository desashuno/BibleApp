package org.biblestudio.ui.i18n

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import biblestudio.composeapp.generated.resources.Res
import biblestudio.composeapp.generated.resources.app_name
import kotlin.test.Test
import org.biblestudio.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalTestApi::class)
class LocalizedStringTest {

    @Test
    fun englishStringResolvesCorrectly() = runComposeUiTest {
        setContent {
            AppTheme {
                // Use the standard stringResource to verify the resource system works
                val appName = stringResource(Res.string.app_name)
                Text(text = appName)
            }
        }

        onNodeWithText("BibleStudio").assertIsDisplayed()
    }
}
