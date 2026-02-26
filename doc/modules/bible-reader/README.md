# Bible Reader

> Module documentation for **Bible Reader** in BibleStudio.

---

## 1. Overview

<!-- Primary Bible reader with parallel view, continuous chapter scrolling, inline highlights and notes, reading modes. Includes text comparison sub-feature (multiple versions side-by-side). -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.bibleReader` |
| **Category** | Read |
| **Accent color** | Calm Blue `#5B7FA3` |
| **Icon** | `Icons.menu_book` |

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
| **Text Comparison** | missing-p0/text-comparison-tool | Side-by-side comparison of multiple Bible translations integrated as a reader mode |

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
