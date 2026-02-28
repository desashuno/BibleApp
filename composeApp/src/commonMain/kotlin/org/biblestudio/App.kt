package org.biblestudio

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.biblestudio.core.navigation.RootChild
import org.biblestudio.core.navigation.RootComponent
import org.biblestudio.core.navigation.RootConfig
import org.biblestudio.features.import_export.component.ImportExportComponent
import org.biblestudio.features.settings.component.SavedLayout
import org.biblestudio.features.settings.component.SettingsComponent
import org.biblestudio.features.settings.component.ThemeMode
import org.biblestudio.features.workspace.component.WorkspaceComponent
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.ui.layout.AdaptiveShell
import org.biblestudio.ui.layout.WindowSizeClass
import org.biblestudio.ui.panes.ImportExportScreen
import org.biblestudio.ui.panes.SettingsScreen
import org.biblestudio.ui.theme.AppTheme
import org.biblestudio.ui.theme.LocalAppFontSize
import org.biblestudio.ui.theme.LocalContinuousScroll
import org.biblestudio.ui.theme.LocalParagraphMode
import org.biblestudio.ui.theme.LocalRedLetter
import org.biblestudio.ui.theme.LocalShowVerseNumbers
import org.biblestudio.ui.workspace.WorkspaceCallbacks
import org.biblestudio.ui.workspace.WorkspaceShell
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun App() {
    // Shared lifecycle for all App-level components
    val lifecycle = remember {
        LifecycleRegistry().apply {
            onCreate()
            onStart()
            onResume()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            lifecycle.onPause()
            lifecycle.onStop()
            lifecycle.onDestroy()
        }
    }

    val koin = remember { GlobalContext.get() }

    // SettingsComponent: drives theme/font AND serves the Settings screen
    val settingsComponent = remember {
        koin.get<SettingsComponent> {
            parametersOf(DefaultComponentContext(lifecycle = lifecycle))
        }
    }
    val settings by settingsComponent.state.collectAsState()

    // RootComponent: drives ChildStack navigation (Workspace / Settings / Import)
    val rootComponent = remember {
        koin.get<RootComponent> {
            parametersOf(
                DefaultComponentContext(lifecycle = lifecycle),
                RootConfig.Workspace
            )
        }
    }
    val childStack by rootComponent.childStack.subscribeAsState()

    val darkTheme = when (settings.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    AppTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalAppFontSize provides settings.fontSize,
            LocalShowVerseNumbers provides settings.showVerseNumbers,
            LocalRedLetter provides settings.redLetter,
            LocalParagraphMode provides settings.paragraphMode,
            LocalContinuousScroll provides settings.continuousScroll,
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AdaptiveShell { sizeClass ->
                    when (childStack.active.instance) {
                        is RootChild.Workspace -> WorkspaceNavScreen(
                            rootComponent = rootComponent,
                            settingsComponent = settingsComponent,
                            sizeClass = sizeClass,
                            lifecycle = lifecycle
                        )

                        is RootChild.Settings -> SettingsNavScreen(
                            rootComponent = rootComponent,
                            settingsComponent = settingsComponent
                        )

                        is RootChild.Import -> ImportNavScreen(
                            rootComponent = rootComponent,
                            lifecycle = lifecycle
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Private navigation screen wrappers
// ---------------------------------------------------------------------------

/**
 * Workspace screen: creates a [WorkspaceComponent], wires [WorkspaceCallbacks],
 * and renders [WorkspaceShell] with the responsive [sizeClass].
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun WorkspaceNavScreen(
    rootComponent: RootComponent,
    settingsComponent: SettingsComponent,
    sizeClass: WindowSizeClass,
    lifecycle: LifecycleRegistry
) {
    val koin = remember { GlobalContext.get() }
    val wsComponent = remember {
        koin.get<WorkspaceComponent> {
            parametersOf(DefaultComponentContext(lifecycle = lifecycle))
        }
    }
    val settings by settingsComponent.state.collectAsState()

    LaunchedEffect(Unit) {
        wsComponent.loadActiveWorkspace {
            settingsComponent.loadLayouts()
        }
        // Safety net: reload layouts after a short delay to cover timing races
        @Suppress("MagicNumber")
        delay(500L)
        settingsComponent.loadLayouts()
    }

    val callbacks = remember(wsComponent, rootComponent, settingsComponent) {
        WorkspaceCallbacks(
            onPaneSelected = wsComponent::addPane,
            onPaneClose = wsComponent::removePane,
            onPaneCloseAtPath = wsComponent::removePaneAtPath,
            onSplitHorizontal = { paneType ->
                wsComponent.splitPane(paneType, paneType, SplitAxis.Horizontal)
            },
            onSplitVertical = { paneType ->
                wsComponent.splitPane(paneType, paneType, SplitAxis.Vertical)
            },
            onResizeSplit = wsComponent::resizeSplit,
            onSwitchTab = wsComponent::switchTab,
            onReorderTab = wsComponent::reorderTab,
            onRearrangePane = wsComponent::rearrangePane,
            onSettingsClick = { rootComponent.navigateTo(RootConfig.Settings) },
            onTogglePinned = settingsComponent::togglePinned,
            onToggleFavorite = settingsComponent::toggleFavorite,
            onLoadWorkspace = { id ->
                wsComponent.saveWorkspace()
                wsComponent.loadWorkspace(id)
                settingsComponent.activateLayout(id)
            },
            onCreateWorkspace = { name ->
                wsComponent.createWorkspace(name) {
                    settingsComponent.loadLayouts()
                }
            },
            onDeleteWorkspace = { id ->
                wsComponent.deleteWorkspace(id) {
                    settingsComponent.loadLayouts()
                }
            }
        )
    }
    WorkspaceShell(
        stateFlow = wsComponent.state,
        pinnedPanes = settings.pinnedPanes,
        favoritePanes = settings.favoritePanes,
        savedLayouts = settings.savedLayouts,
        sizeClass = sizeClass,
        callbacks = callbacks,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Settings screen with a back-arrow toolbar for desktop navigation.
 * Reuses the App-level [settingsComponent] that already drives theme/font.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsNavScreen(
    rootComponent: RootComponent,
    settingsComponent: SettingsComponent
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BackToolbar(onBack = rootComponent::onBack)
        SettingsScreen(
            stateFlow = settingsComponent.state,
            onFontSizeChanged = settingsComponent::setFontSize,
            onThemeChanged = settingsComponent::setTheme,
            onDefaultBibleChanged = settingsComponent::setDefaultBible,
            onSaveLayout = settingsComponent::saveLayout,
            onDeleteLayout = settingsComponent::deleteLayout,
            onActivateLayout = settingsComponent::activateLayout,
            onShowVerseNumbersChanged = settingsComponent::setShowVerseNumbers,
            onRedLetterChanged = settingsComponent::setRedLetter,
            onParagraphModeChanged = settingsComponent::setParagraphMode,
            onContinuousScrollChanged = settingsComponent::setContinuousScroll,
            onSidebarCollapsedChanged = settingsComponent::setSidebarCollapsed,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Import/Export screen with a back-arrow toolbar.
 * Creates its own [ImportExportComponent] from Koin.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun ImportNavScreen(
    rootComponent: RootComponent,
    lifecycle: LifecycleRegistry
) {
    val koin = remember { GlobalContext.get() }
    val component = remember {
        koin.get<ImportExportComponent> {
            parametersOf(DefaultComponentContext(lifecycle = lifecycle))
        }
    }
    val state by component.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        BackToolbar(onBack = rootComponent::onBack)
        ImportExportScreen(
            backups = state.backupHistory,
            isExporting = state.isLoading,
            onExport = component::exportData,
            onCreateBackup = component::createBackup,
            onRestore = { backup -> component.restoreBackup(backup.filename) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Reusable back-arrow toolbar row for full-screen sub-pages (Settings, Import).
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun BackToolbar(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}
