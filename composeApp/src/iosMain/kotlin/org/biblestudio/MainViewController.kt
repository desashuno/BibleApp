package org.biblestudio

import androidx.compose.ui.window.ComposeUIViewController
import org.biblestudio.di.initKoin

/**
 * iOS entry point.
 * The actual iOS app shell is provided by the Xcode project (iosApp/)
 * which hosts a ComposeUIViewController calling App().
 */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun MainViewController(): platform.UIKit.UIViewController {
    initKoin()
    return ComposeUIViewController {
        App()
    }
}
