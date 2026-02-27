# Passage Guide

> Module documentation for **Passage Guide** in BibleStudio.

---

## 1. Overview

The Passage Guide module aggregates study information from multiple other modules into a single, unified panel for a selected verse or passage. It acts as a "study hub" that pulls data from cross-references, word study, morphology, resource-library (commentaries/dictionaries), and notes into a scrollable report per passage.

Rather than owning its own data, the Passage Guide orchestrates queries across other module repositories and presents the results in collapsible sections. This makes it the module with the most consumed dependencies in the system.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.passageGuide` |
| **Category** | Study |
| **Accent color** | Muted Purple `#6B5B8A` (`paneStudy`) |
| **Icon** | `Icons.menu_book` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | N/A (no own tables) |
| Component(s) | Stub |
| UI / Pane | Stub |
| Tests | None |
| i18n | Partial (EN/ES) |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `bible-reader` | Verse Bus + Data | Subscribes to `VerseSelected`; reads verse text |
| `cross-references` | Data | Reads cross-references for passage section |
| `word-study` | Data | Reads key word studies for passage |
| `morphology-interlinear` | Data | Reads morphology data for passage |
| `resource-library` | Data | Reads commentary entries for passage |
| `note-editor` | Data | Reads user notes attached to passage |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| (none) | — | Passage Guide is a leaf consumer |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/passageguide/component/PassageGuideComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/passageguide/component/DefaultPassageGuideComponent.kt` | Logic | Decompose implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/passageguide/ui/PassageGuidePane.kt` | UI | Main pane |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/passageguide/ui/GuideSection.kt` | UI | Collapsible section component |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, repositories (consumed) |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Pending improvements |
