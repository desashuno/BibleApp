# Settings — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("settings") { config ->
    SettingsPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `settings` |
| **Builder** | `SettingsPane` |
| **Category** | Tool |

---

## 2. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `SettingsPane` | `composeApp/.../features/settings/ui/SettingsPane.kt` | Main settings screen | No |
| `SettingsSection` | `composeApp/.../features/settings/ui/SettingsSection.kt` | Grouped section header | Yes |
| `ThemeSelector` | `composeApp/.../features/settings/ui/ThemeSelector.kt` | Light/Dark/System toggle | Yes |
| `FontSizeSlider` | `composeApp/.../features/settings/ui/FontSizeSlider.kt` | Font size slider with preview | Yes |
| `LanguageSelector` | `composeApp/.../features/settings/ui/LanguageSelector.kt` | Locale dropdown | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [Gear] Settings                  [..][X] |  <- Pane Header
+------------------------------------------+
|                                          |
| Appearance                               |
| --------                                 |
| Theme           [Light] [Dark] [System]  |
| Font size       [===o========] 16pt      |
| Font family     [Default          v]     |
|                                          |
| Reading                                  |
| --------                                 |
| Default Bible   [KJV              v]     |
| Text direction  [LTR] [RTL]             |
|                                          |
| Accessibility                            |
| --------                                 |
| High contrast   [off]                    |
|                                          |
| Sync                                     |
| --------                                 |
| Enable sync     [off]                    |
|                                          |
| [Reset to Defaults]                      |
|                                          |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen scrollable settings list |
| **Medium** (600-839dp) | Two-column layout: sections on left, controls on right |
| **Expanded** (840dp+) | Workspace pane with wide section layout |

---

## 5. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loaded** | Settings sections with current values | Always (on mount) |
| **Saving** | Brief saving indicator on changed control | Setting update |
| **Error** | Error message near failed control | DB write failure |

---

## 6. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Section headings | Each `SettingsSection` uses `heading` semantics |
| Slider label | Font size slider: "Font size: 16 points" |
| Theme toggle | "Theme: Light, Dark, System" radio group |
| Reset button | "Reset all settings to defaults" with confirmation dialog |
