# Morphology / Interlinear

> Module documentation for **Morphology / Interlinear** in BibleStudio.

---

## 1. Overview

<!-- Morphological and interlinear analysis with inline display in the reader. Includes reverse interlinear sub-feature. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.morphologyInterlinear` |
| **Category** | Study |
| **Accent color** | Scholarly Purple `#7B6BA3` |
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
| **Reverse Interlinear** | missing-p0/reverse-interlinear | Shows original-language words aligned under English/Spanish text for readers without Greek/Hebrew knowledge |

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
