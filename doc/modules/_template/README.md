# {Module Name}

> Documentation for the **{module}** module of BibleStudio.

---

## 1. Overview

<!-- Brief description of the module's purpose and core functionality (2-3 paragraphs). -->

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.{module}` |
| **Category** | {Read / Study / Write / Tools / Resources / Media} |
| **Accent color** | {Hex from DESIGN_SYSTEM §2.2 pane category token} |
| **Icon** | `Icons.{icon}` |

---

## 3. Current Status

| Aspect | Status |
|--------|--------|
| SQLite schema | {N/A / Defined / Migrations pending} |
| Component(s) | {Not started / Implemented / Partial} |
| UI / Pane | {Not started / Functional / Partial} |
| Tests | {0% / Partial / Covered} |
| i18n | {No / Partial / Complete (EN/ES)} |

---

## 4. Module Dependencies

### 4.1 Consumed (depends on)

<!-- Modules that this module requires to function. -->

| Module | Dependency type | Description |
|--------|----------------|-------------|
| `{module}` | {Route / Verse Bus / Data} | {Why it is needed} |

### 4.2 Provided (depended on by)

<!-- Modules that consume functionality from this module. -->

| Module | Dependency type | Description |
|--------|----------------|-------------|
| `{module}` | {Route / Verse Bus / Data} | {What is provided} |

---

## 5. Key Source Files

| File | Layer | Purpose |
|------|-------|---------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/{module}/component/Default{Module}Component.kt` | Logic | Decompose component |
| `shared/src/commonMain/kotlin/org/biblestudio/features/{module}/component/{Module}Component.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/{module}/model/{Entity}.kt` | Model | Domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/{module}/repository/{Module}Repository.kt` | Data | Repository interface |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/{module}/ui/{Module}Pane.kt` | UI | Composable pane |

---

## Module Documents

| Document | Content |
|----------|---------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Exposed/consumed routes, Verse Bus, deep links |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories, queries |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes, responsive |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Decompose components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Prioritized pending improvements |
