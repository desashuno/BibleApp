package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.settings.component.SavedLayout
import org.biblestudio.features.settings.component.SettingsState
import org.biblestudio.features.settings.component.ThemeMode
import org.biblestudio.ui.theme.Spacing

private const val MIN_FONT_SIZE = 12f
private const val MAX_FONT_SIZE = 28f

/**
 * Settings screen: grouped preference tiles for font size, theme, default Bible,
 * and workspace layout management.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun SettingsScreen(
    stateFlow: StateFlow<SettingsState>,
    onFontSizeChanged: (Int) -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    onDefaultBibleChanged: (String) -> Unit,
    onSaveLayout: (String) -> Unit,
    onDeleteLayout: (String) -> Unit,
    onActivateLayout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(Spacing.Space16)
        )
        HorizontalDivider()

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Display section
                item { SectionHeader("Display") }

                item {
                    FontSizeSlider(
                        currentSize = state.fontSize,
                        onSizeChanged = onFontSizeChanged
                    )
                }

                item {
                    ThemeSelector(
                        currentTheme = state.theme,
                        onThemeChanged = onThemeChanged
                    )
                }

                // Module section
                item { SectionHeader("Modules") }

                item {
                    SettingRow(
                        title = "Default Bible",
                        subtitle = state.defaultBible.ifBlank { "Not set" },
                        onClick = { onDefaultBibleChanged(state.defaultBible) }
                    )
                }

                // Workspace layout management
                item { SectionHeader("Workspace Layouts") }

                item {
                    WorkspaceLayoutManager(
                        layouts = state.savedLayouts,
                        onSave = onSaveLayout,
                        onDelete = onDeleteLayout,
                        onActivate = onActivateLayout
                    )
                }

                // Dynamic groups from database
                state.groups.forEach { group ->
                    item { SectionHeader(group.category) }
                    items(group.settings) { setting ->
                        SettingRow(
                            title = setting.key,
                            subtitle = setting.value
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = Spacing.Space16,
            end = Spacing.Space16,
            top = Spacing.Space24,
            bottom = Spacing.Space8
        )
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FontSizeSlider(currentSize: Int, onSizeChanged: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.Space16)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Font Size",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$currentSize",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentSize.toFloat(),
            onValueChange = { onSizeChanged(it.roundToInt()) },
            valueRange = MIN_FONT_SIZE..MAX_FONT_SIZE,
            steps = (MAX_FONT_SIZE - MIN_FONT_SIZE).toInt() - 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ThemeSelector(currentTheme: ThemeMode, onThemeChanged: (ThemeMode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = Spacing.Space8)
        )
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = currentTheme == mode,
                    onCheckedChange = { if (it) onThemeChanged(mode) }
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun WorkspaceLayoutManager(
    layouts: List<SavedLayout>,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit,
    onActivate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.Space16)) {
        var newLayoutName by remember { mutableStateOf("") }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newLayoutName,
                onValueChange = { newLayoutName = it },
                label = { Text("Layout name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Button(
                onClick = {
                    if (newLayoutName.isNotBlank()) {
                        onSave(newLayoutName.trim())
                        newLayoutName = ""
                    }
                },
                modifier = Modifier.padding(start = Spacing.Space8)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Space8))

        if (layouts.isEmpty()) {
            Text(
                text = "No saved layouts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            layouts.forEach { layout ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Space4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = layout.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (layout.isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        if (layout.isActive) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (!layout.isActive) {
                        OutlinedButton(
                            onClick = { onActivate(layout.id) },
                            modifier = Modifier.padding(start = Spacing.Space4)
                        ) {
                            Text("Load")
                        }
                    }
                    OutlinedButton(
                        onClick = { onDelete(layout.id) },
                        modifier = Modifier.padding(start = Spacing.Space4)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingRow(title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod }
        .padding(horizontal = Spacing.Space16, vertical = Spacing.Space12)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
