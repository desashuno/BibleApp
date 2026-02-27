# Word Study

> Module documentation for **Word Study** in BibleStudio.

---

## 1. Overview

The Word Study module provides in-depth analysis of individual words in the original biblical languages (Hebrew and Greek) using Strong's Concordance data. For any selected Strong's number, it displays the lexicon entry (lemma, transliteration, pronunciation, definition, usage notes), occurrence frequency across all books, and a list of every verse where the word appears.

The module subscribes to `StrongsSelected` events from the VerseBus, typically triggered when a user taps a linked word in the Morphology/Interlinear or Bible Reader panes. Users can also search for Strong's numbers directly within the module.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.wordStudy` |
| **Category** | Study |
| **Accent color** | Muted Purple `#6B5B8A` (`paneStudy`) |
| **Icon** | `Icons.text_fields` |

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
| `morphology-interlinear` | Verse Bus | Receives `StrongsSelected` when user taps interlinear word |
| `bible-reader` | Verse Bus | Receives `StrongsSelected` from HTML word links |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `passage-guide` | Data | Passage guide includes key word studies section |
| `exegetical-guide` | Data | Exegetical guide lexical section uses word study data |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/wordstudy/component/WordStudyComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/wordstudy/component/DefaultWordStudyComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/wordstudy/model/LexiconEntry.kt` | Model | Strong's lexicon domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/wordstudy/repository/WordStudyRepository.kt` | Data | Repository interface |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/wordstudy/ui/WordStudyPane.kt` | UI | Main pane |

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
