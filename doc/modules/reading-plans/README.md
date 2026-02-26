# Reading Plans

> Module documentation for **Reading Plans** in BibleStudio.

---

## 1. Overview

<!-- Reading plans with daily scheduling, progress tracking, reminders, predefined and custom plans. -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.readingPlans` |
| **Category** | Read |
| **Accent color** | Calm Blue `#5B7FA3` |
| **Icon** | `Icons.calendar_today` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | Defined (`reading_plans`, `reading_plan_progress`) |
| Component(s) | Not started |
| UI / Pane | Not started |
| Tests | 0% |
| i18n | No |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `bible-reader` | Route | Opens the reader to the daily passage |
| `bookmarks-history` | Data | Records reading progress |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `dashboard` | Data | Active plan progress widget |

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
