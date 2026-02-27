package org.biblestudio

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.biblestudio.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "${AppInfo.NAME} v${AppInfo.VERSION}"
        ) {
            App()
        }
    }
}
