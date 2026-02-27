# Settings — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 AppSettings

```kotlin
data class AppSettings(
    val theme: ThemeMode = ThemeMode.System,
    val locale: String = "en",
    val fontSize: Float = 16f,
    val fontFamily: String = "default",
    val defaultBibleId: Long = 1,
    val textDirection: TextDirection = TextDirection.Ltr,
    val syncEnabled: Boolean = false,
    val accessibilityHighContrast: Boolean = false,
)

enum class ThemeMode { Light, Dark, System }
```

### 1.2 SettingEntry (raw)

```kotlin
data class SettingEntry(
    val key: String,
    val value: String,
    val type: String,
    val category: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `settings`

```sql
CREATE TABLE settings (
    key      TEXT NOT NULL PRIMARY KEY,
    value    TEXT NOT NULL,
    type     TEXT NOT NULL DEFAULT 'string',
    category TEXT NOT NULL DEFAULT 'general'
);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): Result<AppSettings>
    suspend fun updateSetting(key: String, value: String): Result<Unit>
    suspend fun resetToDefaults(): Result<Unit>
}
```

Note: `observeSettings()` returns a `Flow<AppSettings>` that emits whenever any setting changes, using SQLDelight's `.asFlow().mapToList()` pattern.

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `allSettings` | `Settings.sq` | -- | `List<SettingEntry>` | Full scan (~20 rows max) |
| `settingByKey` | `Settings.sq` | `key: String` | `SettingEntry?` | O(1) PK |
| `upsertSetting` | `Settings.sq` | `key, value, type, category` | `Unit` | O(1) |
| `deleteAllSettings` | `Settings.sq` | -- | `Unit` | Full delete |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v1 | Created `settings` table | Initial schema |

---

## 6. Relations with Other Modules

Settings is a **globally consumed** module. All other modules may read settings but none write to them (except Settings itself). The `observeSettings()` Flow is collected by the root component to apply theme/font changes globally.
