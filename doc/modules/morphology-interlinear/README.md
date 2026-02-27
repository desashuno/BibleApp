# Morphology / Interlinear

> Module documentation for **Morphology / Interlinear** in BibleStudio.

---

## 1. Overview

The Morphology / Interlinear module provides detailed linguistic analysis of biblical text at the word level. For any selected verse, it displays the original-language words (Hebrew or Greek) in an interlinear grid showing the surface form, transliteration, Strong's number, grammatical parsing, and English gloss.

Three display modes are supported: **Interlinear** (vertically stacked rows per word), **Parallel** (side-by-side original and translation), and **Inline** (tooltip popovers on hover/tap). The module includes a parsing decoder that converts morphology codes (e.g. `V-AAI-3S`) into human-readable descriptions.

The module subscribes to `VerseSelected` events and publishes `StrongsSelected` when a user taps a word, enabling seamless navigation to the Word Study pane.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.morphology` |
| **Category** | Study |
| **Accent color** | Muted Purple `#6B5B8A` (`paneStudy`) |
| **Icon** | `Icons.translate` |

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
| `bible-reader` | Verse Bus | Subscribes to `VerseSelected` to load morphology data |
| `word-study` | Data | Shares `lexicon_entries` and `word_occurrences` tables |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `word-study` | Verse Bus | Publishes `StrongsSelected` on word tap |
| `passage-guide` | Data | Passage guide may reference morphology analysis |
| `exegetical-guide` | Data | Exegetical guide grammatical section uses morphology |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/morphology/component/InterlinearComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/morphology/component/DefaultInterlinearComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/morphology/model/MorphologyData.kt` | Model | MorphWord domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/morphology/repository/MorphologyRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/morphology/util/ParsingDecoder.kt` | Logic | Morphology code decoder |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/morphology/ui/InterlinearPane.kt` | UI | Main pane |

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
