# Knowledge Graph — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Entity schema + seed data | Create tables + import biblical entities | 3 days | Phase 1 DB |
| 2 | `KnowledgeGraphRepository` | CRUD + search + verse lookup | 2 days | Schema |
| 3 | Entity browser UI | Category tabs + entity list | 2 days | Repository |
| 4 | Entity detail view | Attributes + verse refs | 1.5 days | Repository |
| 5 | Relationship graph | Force-directed graph renderer | 3 days | Data model |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | FTS search for entities | Full-text search on name/description | 1 day | FTS5 |
| 2 | Cross-link to atlas/timeline | Navigate to related modules | 1.5 days | Those modules |
| 3 | User-created entities | Allow user to add custom entities | 2 days | Repository |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| People browser | Planned | P0 |
| Bible places | Planned | P0 |
| Relationship graph | Planned | P0 |
| Event browser | Planned | P0 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 11.5 days | Phase 1 DB |
| **Phase 6** | 4.5 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
