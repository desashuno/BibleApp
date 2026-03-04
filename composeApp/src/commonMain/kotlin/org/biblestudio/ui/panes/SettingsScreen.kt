package org.biblestudio.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.AppInfo
import org.biblestudio.features.settings.component.SavedLayout
import org.biblestudio.features.settings.component.SettingsState
import org.biblestudio.features.settings.component.ThemeMode
import org.biblestudio.ui.components.SectionHeader
import org.biblestudio.ui.theme.IconSize
import org.biblestudio.ui.components.SettingRow
import org.biblestudio.ui.components.ToggleRow
import org.biblestudio.ui.theme.Spacing

private const val MIN_FONT_SIZE = 12f
private const val MAX_FONT_SIZE = 28f
private val SIDEBAR_WIDTH = 220.dp

private enum class SettingsCategory(
    val label: String,
    val icon: ImageVector
) {
    APPEARANCE("Appearance", Icons.Default.Palette),
    READING("Reading", Icons.AutoMirrored.Filled.MenuBook),
    WORKSPACE("Workspace", Icons.Default.Workspaces),
    DATA("Data", Icons.Default.Storage),
    ABOUT("About", Icons.Default.Info)
}

/**
 * VS Code-style settings screen with a left navigation sidebar
 * and right content area.
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
    onShowVerseNumbersChanged: ((Boolean) -> Unit)? = null,
    onRedLetterChanged: ((Boolean) -> Unit)? = null,
    onParagraphModeChanged: ((Boolean) -> Unit)? = null,
    onContinuousScrollChanged: ((Boolean) -> Unit)? = null,
    onSidebarCollapsedChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    var selectedCategory by remember { mutableStateOf(SettingsCategory.APPEARANCE) }

    Row(modifier = modifier.fillMaxSize()) {
        // Left sidebar navigation
        SettingsSidebar(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        // Vertical divider
        Box(
            modifier = Modifier
                .width(Spacing.Divider)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // Right content area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.Space24)
        ) {
            Text(
                text = selectedCategory.label,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.Space16))

            when (selectedCategory) {
                SettingsCategory.APPEARANCE -> AppearanceSection(
                    state = state,
                    onFontSizeChanged = onFontSizeChanged,
                    onThemeChanged = onThemeChanged
                )
                SettingsCategory.READING -> ReadingSection(
                    state = state,
                    onDefaultBibleChanged = onDefaultBibleChanged,
                    onShowVerseNumbersChanged = onShowVerseNumbersChanged,
                    onRedLetterChanged = onRedLetterChanged,
                    onParagraphModeChanged = onParagraphModeChanged,
                    onContinuousScrollChanged = onContinuousScrollChanged
                )
                SettingsCategory.WORKSPACE -> WorkspaceSection(
                    state = state,
                    onSaveLayout = onSaveLayout,
                    onDeleteLayout = onDeleteLayout,
                    onActivateLayout = onActivateLayout,
                    onSidebarCollapsedChanged = onSidebarCollapsedChanged
                )
                SettingsCategory.DATA -> DataSection()
                SettingsCategory.ABOUT -> AboutSection()
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsSidebar(selectedCategory: SettingsCategory, onCategorySelected: (SettingsCategory) -> Unit) {
    Column(
        modifier = Modifier
            .width(SIDEBAR_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(vertical = Spacing.Space8)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.Space16, vertical = Spacing.Space12)
        )

        SettingsCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(category) }
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = Spacing.Space16, vertical = Spacing.Space12)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(IconSize.Default)
                )
                Spacer(Modifier.width(Spacing.Space12))
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

// ── Appearance Section ──

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun AppearanceSection(
    state: SettingsState,
    onFontSizeChanged: (Int) -> Unit,
    onThemeChanged: (ThemeMode) -> Unit
) {
    // Theme selector cards
    SectionHeader("Theme")
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space12),
        modifier = Modifier.padding(bottom = Spacing.Space16)
    ) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = state.theme == mode
            val label = when (mode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System"
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(Spacing.Space12),
                modifier = Modifier.clickable { onThemeChanged(mode) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Space24,
                        vertical = Spacing.Space16
                    )
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }

    // Font size slider
    SectionHeader("Font Size")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Space8)
    ) {
        Text(
            text = "Reading text size",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${state.fontSize}sp",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Slider(
        value = state.fontSize.toFloat(),
        onValueChange = { onFontSizeChanged(it.roundToInt()) },
        valueRange = MIN_FONT_SIZE..MAX_FONT_SIZE,
        steps = (MAX_FONT_SIZE - MIN_FONT_SIZE).toInt() - 1,
        modifier = Modifier.fillMaxWidth()
    )
    // Preview text
    Text(
        text = "Preview: The Lord is my shepherd",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = state.fontSize.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = Spacing.Space8)
    )
}

// ── Reading Section ──

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ReadingSection(
    state: SettingsState,
    onDefaultBibleChanged: (String) -> Unit,
    onShowVerseNumbersChanged: ((Boolean) -> Unit)?,
    onRedLetterChanged: ((Boolean) -> Unit)?,
    onParagraphModeChanged: ((Boolean) -> Unit)?,
    onContinuousScrollChanged: ((Boolean) -> Unit)?
) {
    SectionHeader("Default Bible")
    SettingRow(
        title = "Default Bible version",
        subtitle = state.defaultBible.ifBlank { "Not set" },
        onClick = { onDefaultBibleChanged(state.defaultBible) }
    )

    SectionHeader("Display Options")

    ToggleRow(
        title = "Show verse numbers",
        subtitle = "Display verse numbers in the reading view",
        checked = state.showVerseNumbers,
        onCheckedChange = { onShowVerseNumbersChanged?.invoke(it) }
    )

    ToggleRow(
        title = "Red-letter words",
        subtitle = "Show words of Jesus in red (when available)",
        checked = state.redLetter,
        onCheckedChange = { onRedLetterChanged?.invoke(it) }
    )

    ToggleRow(
        title = "Paragraph mode",
        subtitle = "Display Bible text as flowing paragraphs instead of verse-per-line",
        checked = state.paragraphMode,
        onCheckedChange = { onParagraphModeChanged?.invoke(it) }
    )

    ToggleRow(
        title = "Continuous scroll",
        subtitle = "Scroll through an entire book with chapter dividers",
        checked = state.continuousScroll,
        onCheckedChange = { onContinuousScrollChanged?.invoke(it) }
    )
}

// ── Workspace Section ──

@Suppress("ktlint:standard:function-naming")
@Composable
private fun WorkspaceSection(
    state: SettingsState,
    onSaveLayout: (String) -> Unit,
    onDeleteLayout: (String) -> Unit,
    onActivateLayout: (String) -> Unit,
    onSidebarCollapsedChanged: ((Boolean) -> Unit)?
) {
    SectionHeader("Sidebar")
    ToggleRow(
        title = "Sidebar collapsed by default",
        subtitle = "Start with icon-only sidebar",
        checked = state.sidebarCollapsed,
        onCheckedChange = { onSidebarCollapsedChanged?.invoke(it) }
    )

    SectionHeader("Saved Layouts")
    WorkspaceLayoutManager(
        layouts = state.savedLayouts,
        onSave = onSaveLayout,
        onDelete = onDeleteLayout,
        onActivate = onActivateLayout
    )
}

// ── Data Section ──

@Suppress("ktlint:standard:function-naming")
@Composable
private fun DataSection() {
    SectionHeader("Modules")
    Text(
        text = "Use the Module Manager pane to manage Bible modules, dictionaries, and commentaries.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = Spacing.Space16)
    )

    SectionHeader("Import / Export")
    Text(
        text = "Backup and restore your data, notes, and highlights via the Import/Export screen.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ── About Section ──

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AboutSection() {
    SectionHeader("Application")
    SettingRow(title = "BibleStudio", subtitle = "Version ${AppInfo.VERSION}")
    SettingRow(title = "Database", subtitle = "Schema version ${AppInfo.DATABASE_VERSION}")

    SectionHeader("License")
    Text(
        text = "BibleStudio is free and open-source software. " +
            "All Bible data sources are public domain or Creative Commons licensed.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ── Shared components are imported from org.biblestudio.ui.components ──

@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun WorkspaceLayoutManager(
    layouts: List<SavedLayout>,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit,
    onActivate: (String) -> Unit
) {
    var newLayoutName by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = newLayoutName,
            onValueChange = { newLayoutName = it },
            label = { Text("Layout name") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Spacing.Space8))
        Button(
            onClick = {
                if (newLayoutName.isNotBlank()) {
                    onSave(newLayoutName.trim())
                    newLayoutName = ""
                }
            }
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
