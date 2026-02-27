# Cross-References

> Module documentation for **Cross-References** in BibleStudio.

---

## 1. Overview

The Cross-References module displays Scripture cross-references and parallel passages for any selected verse. It integrates the Treasury of Scripture Knowledge (TSK) dataset (~2.1 MB bundled) providing comprehensive verse-to-verse links with type classification (parallel, quotation, allusion) and confidence scores.

The module subscribes to `VerseSelected` events from the VerseBus and automatically loads references for the active verse. Users can tap any reference to navigate the Bible Reader to the target verse, creating a seamless study workflow.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.crossReferences` |
| **Category** | Study |
| **Accent color** | Muted Purple `#6B5B8A` (`paneStudy`) |
| **Icon** | `Icons.link` |

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
| `bible-reader` | Verse Bus | Subscribes to `VerseSelected` to load references |
| `bible-reader` | Data | Reads target verse text for preview snippets |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `passage-guide` | Data | Passage guide aggregates cross-references section |
| `exegetical-guide` | Data | Exegetical guide includes theological cross-refs |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/component/CrossReferenceComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/component/DefaultCrossReferenceComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/model/CrossReference.kt` | Model | CrossReference domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/model/ParallelPassage.kt` | Model | ParallelPassage domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/repository/CrossRefRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/crossreferences/repository/CrossRefRepositoryImpl.kt` | Data | SQLDelight implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/crossreferences/ui/CrossReferencePane.kt` | UI | Main pane composable |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories, queries |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Pending improvements |
