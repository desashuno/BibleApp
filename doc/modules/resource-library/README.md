# Resource Library

> Module documentation for **Resource Library** in BibleStudio.

---

## 1. Overview

The Resource Library module manages external study resources: commentaries, Bible dictionaries, devotionals, and other reference works. Each resource is a metadata record (`resources` table) with per-verse entries (`resource_entries` table), enabling verse-linked content lookup.

Resources are imported from bundled or user-downloaded packages and indexed for full-text search via the `fts_resources` FTS5 virtual table. The module supports multiple resource types (commentary, dictionary, devotional, map, chart) and exposes a `ResourceRepository` used by both the Passage Guide and Search modules.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.resourceLibrary` |
| **Category** | Resources |
| **Accent color** | Olive `#6E7A5B` (`paneResource`) |
| **Icon** | `Icons.library_books` |

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
| `bible-reader` | Verse Bus | Subscribes to `VerseSelected` to load resource entries |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `passage-guide` | Data | Passage guide reads commentary for verse |
| `search` | Data (FTS5) | Search queries `fts_resources` |
| `exegetical-guide` | Data | Exegetical guide reads commentary/dictionary |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/component/ResourceLibraryComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/component/DefaultResourceLibraryComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/model/Resource.kt` | Model | Resource metadata entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/model/ResourceEntry.kt` | Model | Per-verse entry entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/repository/ResourceRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/resources/repository/ResourceRepositoryImpl.kt` | Data | SQLDelight implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/resources/ui/ResourceLibraryPane.kt` | UI | Main pane |

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
