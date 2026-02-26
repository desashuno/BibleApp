# Dashboard

> Module documentation for **Dashboard** in BibleStudio.

---

## 1. Overview

<!-- Personalized home screen with quick-access widgets: active reading plan, recent notes, history, verse of the day, shortcuts to saved workspaces. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.dashboard` |
| **Category** | Tools |
| **Accent color** | Utility Amber `#A38B5B` |
| **Icon** | `Icons.home` |

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
| `reading-plans` | Data | Active reading plan progress |
| `note-editor` | Data | Recent notes |
| `bookmarks-history` | Data | Recent navigation history |
| `workspace` | Route | Quick access to saved workspaces |

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
