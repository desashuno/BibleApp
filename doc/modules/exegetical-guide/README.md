# Exegetical Guide

> Module documentation for **Exegetical Guide** in BibleStudio.

---

## 1. Overview

<!-- Exegetical guide that assembles grammatical analysis, lexical data, cross-references, and commentaries into a scrollable per-passage report. Key differentiator vs Logos Bible Software. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.exegeticalGuide` |
| **Category** | Study |
| **Accent color** | Scholarly Purple `#7B6BA3` |
| **Icon** | `Icons.school` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | Not started |
| Component(s) | Not started |
| UI / Pane | Not started |
| Tests | 0% |
| i18n | No |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `morphology-interlinear` | Data | Grammatical and morphological analysis |
| `word-study` | Data | Lexical data and semantic domains |
| `cross-references` | Data | Passage cross-references |
| `resource-library` | Data | Commentaries and resources |
| `passage-guide` | Route | Navigation to related passage guide |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| <!-- TBD --> | | |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| <!-- TBD --> | | |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed for inter-module communication |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories, queries |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, screens, PaneRegistry registration |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Decompose components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Prioritized pending improvements |
