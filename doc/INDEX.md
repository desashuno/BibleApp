# BibleStudio — Technical Documentation

> Documentation hub for the BibleStudio project.
> **Stack**: Kotlin Multiplatform · Compose Multiplatform · SQLDelight · Decompose · Koin

---

## Core Documents

| Document | Content | Audience |
|----------|---------|----------|
| [**ARCHITECTURE.md**](ARCHITECTURE.md) | System architecture, KMP layers, Koin DI, data flow, sealed-class error handling, Napier logging, Decompose routing, bootstrap sequence | All developers |
| [**DATA_LAYER.md**](DATA_LAYER.md) | SQLDelight `.sq` files, 27 tables, 5 FTS5 tables, 8 query groups, 17 repositories, global verse IDs, reactive queries, migrations, sync | Backend developers |
| [**DESIGN_SYSTEM.md**](DESIGN_SYSTEM.md) | Digital Scriptorium identity, color palette, Compose Multiplatform theming, Typography, animations, components, responsive breakpoints, i18n (EN+ES), accessibility | UI/UX developers, designers |
| [**PLATFORM_STRATEGY.md**](PLATFORM_STRATEGY.md) | 5-platform matrix, expect/actual abstractions, storage paths, file pickers, Gradle build commands, jpackage packaging, release checklists | All developers, DevOps |
| [**MODULE_SYSTEM.md**](MODULE_SYSTEM.md) | PaneRegistry, 21-module catalog, Verse Bus (SharedFlow + sealed LinkEvent), LayoutNode, workspace presets, module creation guide | Feature developers |
| [**CODE_CONVENTIONS.md**](CODE_CONVENTIONS.md) | Feature-first structure, Kotlin naming conventions, Decompose component pattern, entity pattern, import rules, SQLDelight conventions, Composable conventions | All developers |
| [**GETTING_STARTED.md**](GETTING_STARTED.md) | Prerequisites (JDK 17+, Gradle), setup steps, Gradle commands reference, debugging, first contribution guide, branch strategy | New developers |
| [**TESTING.md**](TESTING.md) | Test pyramid, tools (Turbine, MockK, SQLDelight in-memory driver, Compose UI Test), patterns for Component/Query/Repository/Composable/Migration tests, coverage targets (80%+) | All developers |
| [**CI_CD.md**](CI_CD.md) | GitHub Actions workflow (Gradle-based), quality gates (detekt, ktlint, Kover), branch protection, release process, versioning | DevOps, maintainers |
| [**SECURITY.md**](SECURITY.md) | Threat model, data protection, user data inventory, import validation, SQL safety (SQLDelight parameterization), input sanitization, platform permissions, dependency security | All developers, security |
| [**OPEN_DATA_SOURCES.md**](OPEN_DATA_SOURCES.md) | Open-source data sources per module, licenses, attribution, data pipeline overview | All developers, data |

---

## Module Documentation

Each module is documented as an independent folder with 7 standardized files.
See the **base template** at [`modules/_template/`](modules/_template/README.md).

### Module Structure

```
modules/{name}/
  ├── README.md          → Overview, category, status, dependencies
  ├── ARCHITECTURE.md    → Internal layers, data flow, DI
  ├── ROUTES.md          → Exposed/consumed routes, Verse Bus, deep links
  ├── DATA_MODEL.md      → Entities, SQLite tables, repositories, queries
  ├── UI_COMPONENTS.md   → Composables, PaneRegistry, wireframes, responsive
  ├── COMPONENT_STATE.md → Decompose components, StateFlow, side effects
  └── ROADMAP.md         → P0/P1/P2 improvements, absorbed features
```

### Read

| Module | Description | Status |
|--------|-------------|--------|
| [`bible-reader`](modules/bible-reader/README.md) | Primary Scripture reader + text comparison (sub-feature) | Functional |
| [`reading-plans`](modules/reading-plans/README.md) | Daily reading plans with progress tracking | New |

### Study

| Module | Description | Status |
|--------|-------------|--------|
| [`cross-references`](modules/cross-references/README.md) | Cross-references with inline rendering | Functional |
| [`word-study`](modules/word-study/README.md) | Word study, usage charts, semantic domains | Functional |
| [`morphology-interlinear`](modules/morphology-interlinear/README.md) | Morphology & interlinear + reverse interlinear (sub-feature) | Functional |
| [`passage-guide`](modules/passage-guide/README.md) | Passage guide with outlines and parallel passages | Functional |
| [`exegetical-guide`](modules/exegetical-guide/README.md) | Exegetical guide: grammatical analysis + lexicon + commentaries | New |
| [`knowledge-graph`](modules/knowledge-graph/README.md) | Biblical knowledge graph (people, places, events) | Schema ready |
| [`timeline`](modules/timeline/README.md) | Interactive timeline | Schema ready |
| [`theological-atlas`](modules/theological-atlas/README.md) | Atlas with interactive maps and overlays | Package ready |

### Write

| Module | Description | Status |
|--------|-------------|--------|
| [`note-editor`](modules/note-editor/README.md) | WYSIWYG note editor with verse anchoring | Functional |
| [`sermon-editor`](modules/sermon-editor/README.md) | Sermon editor with outline mode | Package ready |

### Tools

| Module | Description | Status |
|--------|-------------|--------|
| [`search`](modules/search/README.md) | Full-text search with filters + syntax search (sub-feature) | Functional |
| [`highlights`](modules/highlights/README.md) | Inline highlights with character-level selection | Schema ready |
| [`workspace`](modules/workspace/README.md) | Multi-pane workspace + quickstart layouts (sub-feature) | Functional |
| [`module-system`](modules/module-system/README.md) | Bible data module management | Functional |
| [`import-export`](modules/import-export/README.md) | Import/Export OSIS, USFM, Sword | Functional |
| [`settings`](modules/settings/README.md) | Configuration, preferences, keyboard shortcuts | Functional |
| [`bookmarks-history`](modules/bookmarks-history/README.md) | Bookmarks and navigation history | Schema ready |
| [`dashboard`](modules/dashboard/README.md) | Customizable home screen | New |

### Resources

| Module | Description | Status |
|--------|-------------|--------|
| [`resource-library`](modules/resource-library/README.md) | Commentary, dictionary, and media browser | Functional |

### Media

| Module | Description | Status |
|--------|-------------|--------|
| [`audio-sync`](modules/audio-sync/README.md) | Synchronized audio Bible playback | New |

### Shared Services

| Service | Description | Location |
|---------|-------------|----------|
| [`share-verse`](modules/_shared/share-verse/README.md) | Share verses as text or styled image | `modules/_shared/share-verse/` |

---

## Quick Navigation

### Want to understand the overall architecture?
Start with [ARCHITECTURE.md](ARCHITECTURE.md)

### Building for a specific platform?
Go to [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) §7 — Build Guides

### Need to modify the database?
See [DATA_LAYER.md](DATA_LAYER.md) §3 — Schema, §8 — Migrations

### Creating or modifying a visual component?
Review [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) §2–6 — Colors, Typography, Components

### First day on the project?
Follow [GETTING_STARTED.md](GETTING_STARTED.md) §2 — Setup

### Adding a new pane module?
Follow [MODULE_SYSTEM.md](MODULE_SYSTEM.md) §6 — Module Creation Guide, then the [module template](modules/_template/README.md)

### Understanding a specific module?
Go to `modules/{name}/README.md` — each has complete standalone documentation

---

## Project Statistics

| Metric | Value |
|--------|-------|
| Kotlin source files | ~400+ |
| Documented modules | 22 |
| Shared services | 1 |
| Domain entities | 33 |
| Repository interfaces | 17 |
| SQLDelight query groups | 8 |
| Decompose components | 15+ |
| Composable screens | 25+ |
| Pane types | 21 |
| Reusable composables | 37+ |
| SQLite tables | 27 |
| FTS5 tables | 5 |
| Schema version | 16 |
| Supported languages | 2 (EN, ES) |
| Target platforms | 5 (Android, iOS, Windows, macOS, Linux) |
| Test files | 25+ |

---

## Other Reference Files

| File | Location | Purpose |
|------|----------|---------|
| `CLAUDE.md` | Project root | Quick orientation for AI sessions |
| `README.md` | Project root | Product overview and features |
| `build.gradle.kts` | Project root | Gradle root configuration |
| `settings.gradle.kts` | Project root | Module declarations |
| `gradle.properties` | Project root | KMP & Compose flags |
| `libs.versions.toml` | `gradle/` | Dependency version catalog |
| `detekt.yml` | Project root | Static analysis rules |
| `data-pipeline/` | Project root | Download & normalize open-source Bible data into SQLite |
