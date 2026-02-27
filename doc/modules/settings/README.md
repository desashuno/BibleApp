# Settings

> Module documentation for **Settings** in BibleStudio.

---

## 1. Overview

The Settings module manages all application configuration through a key-value store in the `settings` SQLite table. Settings are organized by category (appearance, reading, sync, accessibility) and exposed as strongly-typed properties via a `SettingsRepository`.

Changes to settings are observed via Kotlin `StateFlow`, enabling real-time UI updates (e.g. font size, theme, text direction) without requiring app restart. The module also manages theme selection (Light/Dark/System) using the Design System tokens.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.settings` |
| **Category** | Tool |
| **Accent color** | Dusty Rose `#7A5A5A` (`paneTool`) |
| **Icon** | `Icons.settings` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | Defined |
| Component(s) | Implemented |
| UI / Pane | Functional |
| Tests | Partial |
| i18n | Partial (EN/ES) |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| (none) | -- | Settings is a root dependency |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `bible-reader` | Data | Font size, text direction, default Bible |
| `workspace` | Data | Default workspace layout, theme |
| All modules | Data | Theme, locale, accessibility settings |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/settings/component/SettingsComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/settings/component/DefaultSettingsComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/settings/model/AppSettings.kt` | Model | Typed settings model |
| `shared/src/commonMain/kotlin/org/biblestudio/features/settings/repository/SettingsRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/settings/repository/SettingsRepositoryImpl.kt` | Data | SQLDelight implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/settings/ui/SettingsPane.kt` | UI | Settings pane |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Pending improvements |
