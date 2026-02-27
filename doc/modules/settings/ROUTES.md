# Settings — Routes & Navigation

> Inter-module communication via Decompose navigation and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `SettingsConfig.Main` | -- | Open settings pane |
| `SettingsConfig.Section(category)` | `category: String` | Open specific settings section |

---

## 2. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("settings", config = mapOf("section" to "appearance"))
```

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://settings` | `biblestudio://settings` | Opens settings pane |
| `biblestudio://settings/{section}` | `biblestudio://settings/appearance` | Opens appearance section |

---

## 4. Verse Bus (LinkEvent)

Settings does **not** participate in VerseBus communication. It is consumed globally via `SettingsRepository.observeSettings()`.

---

## 5. Global Integration

| Consumer | Integration | Description |
|----------|-------------|-------------|
| Root Component | `observeSettings().collect {}` | Applies theme, locale, font globally |
| All Compose screens | `MaterialTheme` | Theme tokens auto-update on setting change |
| Bible Reader | `settings.fontSize`, `settings.textDirection` | Text rendering parameters |
