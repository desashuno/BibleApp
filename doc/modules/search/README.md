# Search

> Module documentation for **Search** in BibleStudio.

---

## 1. Overview

<!-- Search system with book/testament filters, saved searches, result sorting. Includes syntax search sub-feature for morphological patterns. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.search` |
| **Category** | Tools |
| **Accent color** | Utility Amber `#A38B5B` |
| **Icon** | `Icons.search` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | Defined (FTS5) |
| Component(s) | Implemented |
| UI / Pane | Functional |
| Tests | Partial |
| i18n | Partial (EN/ES) |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| <!-- TBD --> | | |

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

## 6. Integrated Sub-features

| Sub-feature | Origin | Description |
|-----------|--------|-------------|
| **Syntax Search** | missing-p0/syntax-search | Advanced search by grammatical and morphological patterns integrated as a search mode |

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
