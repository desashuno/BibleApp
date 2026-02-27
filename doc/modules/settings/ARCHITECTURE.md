# Settings — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  SettingsPane (@Composable)                       |
|  +-- Grouped sections: Appearance, Reading, etc.  |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultSettingsComponent (Decompose)             |
|  +-- Manages StateFlow<SettingsState>             |
|  +-- Calls SettingsRepository on change           |
+---------------------------------------------------+
|                      DATA                         |
|  SettingsRepository (interface)                   |
|  SettingsRepositoryImpl                           |
|  +-- SettingsQueries (SQLDelight)                 |
|       +-- SQLite (settings table)                 |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Read/Update Setting

1. **App startup** -- `SettingsRepository.observeSettings()` emits initial `AppSettings`.
2. **Root component** -- Applies theme, font, locale to Compose `MaterialTheme`.
3. **User changes** -- Settings pane calls `updateSetting(key, value)`.
4. **Flow re-emits** -- All observers receive updated `AppSettings`.
5. **UI recomposes** -- Theme/font/locale change takes effect immediately.

---

## 3. Dependency Injection

```kotlin
val settingsModule = module {
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultSettingsComponent(
            componentContext = ctx,
            repository = get(),
        )
    }
}
```

---

## 4. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `SettingsRepositoryImpl` | Abstracts key-value store |
| Observable Flow | `observeSettings()` | Real-time setting propagation |
| Typed wrapper | `AppSettings` data class | Type-safe access; avoids raw string keys |
| Upsert | `upsertSetting` SQL | INSERT OR REPLACE for atomic updates |

---

## 5. Performance Considerations

- **Settings table**: < 30 rows total; full scan is negligible.
- **Flow observation**: Uses SQLDelight's `asFlow()` which listens to table changes via notify.
- **No caching needed**: Table is small enough to query directly on every read.

---

## 6. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| Key-value table | SharedPreferences/DataStore | Cross-platform; queryable; part of single SQLite DB |
| `observeSettings()` Flow | Callback-based | Compose-native; automatic recomposition |
| Typed `AppSettings` wrapper | Raw key/value access | Compile-time safety; centralized defaults |
