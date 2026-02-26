# Workspace

> Module documentation for **Workspace** in BibleStudio.

---

## 1. Overview

<!-- Multi-pane panel system with tab support, save/restore layouts, workspace presets. Includes quickstart layouts sub-feature with preconfigured layouts for new users. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.workspace` |
| **Category** | Tools |
| **Accent color** | Utility Amber `#A38B5B` |
| **Icon** | `Icons.dashboard` |

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
| <!-- All modules depend on workspace to render as panes --> | | |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| <!-- TBD --> | | |

---

## 6. Integrated Sub-features

| Sub-feature | Origin | Description |
|-----------|--------|-------------|
| **Quickstart Layouts** | missing-p0/quickstart-layouts | Preconfigured layouts for first launch and new users |

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
