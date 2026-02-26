# BibleStudio — Development Roadmap

> Complete development roadmap from project setup to production release.
> **Stack**: Kotlin Multiplatform · Compose Multiplatform · SQLDelight · Decompose · Koin

---

## Overview

BibleStudio is a multi-platform Bible study application targeting Android, iOS, Windows, macOS, and Linux. This roadmap covers the full implementation path from an empty Gradle project to a feature-complete release.

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Shared logic | Kotlin Multiplatform (Kotlin 2.x) |
| UI | Compose Multiplatform (JetBrains) |
| Database | SQLDelight 2.x + SQLite + FTS5 |
| Navigation & lifecycle | Decompose (Arkadii Ivanov) |
| DI | Koin 3.x |
| Async | Kotlin Coroutines + Flow |
| Serialization | kotlinx.serialization |
| Logging | Napier |
| Linting | detekt + ktlint |
| Coverage | Kover |
| CI | GitHub Actions |

### Phase Summary

| Phase | Focus |
|-------|-------|
| [Phase 1](phase-1-foundation.md) | Project foundation — Gradle, targets, schema, DI, app shell |
| [Phase 2](phase-2-infrastructure.md) | Core infrastructure — VerseBus, PaneRegistry, routing, theme, workspace |
| [Phase 3](phase-3-core-modules.md) | Core modules — Bible Reader, Search, Cross-Refs, Settings, Import/Export |
| [Phase 4](phase-4-study-writing.md) | Study & Writing — Word Study, Morphology, Passage Guide, Notes, Highlights |
| [Phase 5](phase-5-advanced.md) | Advanced modules — Knowledge Graph, Timeline, Atlas, Exegetical Guide, Sermons, Reading Plans, Dashboard, Audio |
| [Phase 6](phase-6-platform-release.md) | Platform release — packaging, CI/CD, security audit, performance, accessibility, launch |

### Phase Dependencies

```
Phase 1 (Foundation)
  └──→ Phase 2 (Infrastructure)
         └──→ Phase 3 (Core Modules)
                ├──→ Phase 4 (Study & Writing)
                │      └──→ Phase 5 (Advanced)
                └──→ Phase 6 (Platform Release) ← can start in parallel with Phase 4/5
```

### How to Use This Roadmap

- Each phase file contains detailed `[ ]` checkboxes organized by area
- Mark items `[x]` as you complete them
- Items within a phase can generally be done in any order unless noted
- Cross-phase dependencies are noted inline
- Each checkbox is a single, verifiable unit of work

### Module Status Summary

| Status | Count | Modules |
|--------|-------|---------|
| **Functional** | 12 | bible-reader, cross-references, word-study, morphology-interlinear, passage-guide, note-editor, search, workspace, module-system, import-export, settings, resource-library |
| **Schema Ready** | 4 | knowledge-graph, timeline, highlights, bookmarks-history |
| **Package Ready** | 2 | theological-atlas, sermon-editor |
| **New** | 4 | reading-plans, exegetical-guide, dashboard, audio-sync |

---

## Reference

| Document | Location |
|----------|----------|
| Architecture | [doc/ARCHITECTURE.md](../ARCHITECTURE.md) |
| Data Layer | [doc/DATA_LAYER.md](../DATA_LAYER.md) |
| Module System | [doc/MODULE_SYSTEM.md](../MODULE_SYSTEM.md) |
| Design System | [doc/DESIGN_SYSTEM.md](../DESIGN_SYSTEM.md) |
| Platform Strategy | [doc/PLATFORM_STRATEGY.md](../PLATFORM_STRATEGY.md) |
| Testing | [doc/TESTING.md](../TESTING.md) |
| CI/CD | [doc/CI_CD.md](../CI_CD.md) |
| Code Conventions | [doc/CODE_CONVENTIONS.md](../CODE_CONVENTIONS.md) |
