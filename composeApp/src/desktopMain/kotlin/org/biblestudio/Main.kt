package org.biblestudio

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.MouseInfo
import java.awt.Point
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.di.initKoin
import org.biblestudio.ui.PaneRegistration

private val TitleBarBg = Color(0xFF1C1917)
private val TitleBarText = Color(0xFFD6CFC7)
private val TitleBarAccent = Color(0xFFC4A882)
private val TitleBarHeight = 36.dp
private val TitleBarIconSize = 16.dp
private val WindowControlSize = 28.dp

fun main() {
    initKoin()
    PaneRegistry.init()
    PaneRegistration.registerAll()
    check(PaneRegistry.availableTypes.size >= 22) {
        "PaneRegistry has only ${PaneRegistry.availableTypes.size} types after init"
    }
    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "${AppInfo.NAME} v${AppInfo.VERSION}",
            state = windowState,
            undecorated = true,
            resizable = true
        ) {
            Column(modifier = Modifier.fillMaxSize().background(TitleBarBg)) {
                CustomTitleBar(
                    title = "${AppInfo.NAME} v${AppInfo.VERSION}",
                    windowState = windowState,
                    onMinimize = { windowState.isMinimized = true },
                    onMaximize = {
                        windowState.placement =
                            if (windowState.placement == WindowPlacement.Maximized) {
                                WindowPlacement.Floating
                            } else {
                                WindowPlacement.Maximized
                            }
                    },
                    onClose = ::exitApplication
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FrameWindowScope.CustomTitleBar(
    title: String,
    windowState: WindowState,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit
) {
    // Drag-to-move: track the initial mouse screen position on drag start
    var dragStartMouseScreen = Point(0, 0)
    var dragStartWindowPos = Point(0, 0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TitleBarHeight)
            .background(TitleBarBg)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragStartMouseScreen = MouseInfo.getPointerInfo().location
                        dragStartWindowPos = window.location
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentMouse = MouseInfo.getPointerInfo().location
                        window.setLocation(
                            dragStartWindowPos.x + (currentMouse.x - dragStartMouseScreen.x),
                            dragStartWindowPos.y + (currentMouse.y - dragStartMouseScreen.y)
                        )
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = TitleBarAccent,
                modifier = Modifier.size(TitleBarIconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = TitleBarText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))

            // Window controls
            IconButton(onClick = onMinimize, modifier = Modifier.size(WindowControlSize)) {
                Icon(Icons.Default.Remove, "Minimize", tint = TitleBarText, modifier = Modifier.size(TitleBarIconSize))
            }
            IconButton(onClick = onMaximize, modifier = Modifier.size(WindowControlSize)) {
                Icon(Icons.Default.CropSquare, "Maximize", tint = TitleBarText, modifier = Modifier.size(TitleBarIconSize))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(WindowControlSize)) {
                Icon(Icons.Default.Close, "Close", tint = TitleBarText, modifier = Modifier.size(TitleBarIconSize))
            }
        }
    }
}
