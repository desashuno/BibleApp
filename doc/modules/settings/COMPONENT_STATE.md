# Settings — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultSettingsComponent` | Scoped (per pane) | `shared/.../features/settings/component/DefaultSettingsComponent.kt` | Settings CRUD with reactive Flow |

---

## 2. SettingsComponent

### 2.1 Interface

```kotlin
interface SettingsComponent {
    val state: StateFlow<SettingsState>
    fun onThemeChanged(theme: ThemeMode)
    fun onFontSizeChanged(size: Float)
    fun onFontFamilyChanged(family: String)
    fun onLocaleChanged(locale: String)
    fun onTextDirectionChanged(direction: TextDirection)
    fun onResetDefaults()
}
```

### 2.2 State

```kotlin
data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val isSaving: Boolean = false,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (default settings loaded from DB)
  |
  | onThemeChanged / onFontSizeChanged / etc.
  v
Saving (isSaving=true)
  |
  +-- success --> Updated (settings Flow re-emits; isSaving=false)
  +-- failure --> Error
  |
  | onResetDefaults
  v
Saving (all settings reset to defaults)
  |
  +-- success --> Initial (default settings)
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| Any setting change | DB upsert | Writes to `settings` table |
| DB change | Flow emission | All observers receive updated `AppSettings` |
| `onResetDefaults` | DB delete + re-insert | Clears all settings; defaults re-applied |

---

## 4. Testing

```kotlin
@Test
fun `onThemeChanged updates settings Flow`() = runTest {
    val repository = FakeSettingsRepository()
    val component = DefaultSettingsComponent(
        componentContext = TestComponentContext(),
        repository = repository,
    )
    component.state.test {
        assertThat(awaitItem().settings.theme).isEqualTo(ThemeMode.System)
        component.onThemeChanged(ThemeMode.Dark)
        assertThat(awaitItem().settings.theme).isEqualTo(ThemeMode.Dark)
    }
}
```
