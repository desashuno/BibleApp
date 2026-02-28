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
| 1 | FTS search for entities | Full-text search on name/description via fts_entities | 1 day | FTS5 |
| 2 | Cross-link to atlas/timeline | Tap location entity → center atlas map; tap event → scroll timeline | 1.5 days | Those modules |
| 3 | User-created entities | Allow user to add custom entities and relationships | 2 days | Repository |
| 4 | Entity type icons | Distinct icons for Person, Place, Nation, Object, Event categories | 1 day | UI |
| 5 | Family tree view | Specialized layout for genealogical relationships (patriarchs, kings) | 3 days | Relationship data |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Entity merge/disambiguation | Merge duplicate entities (e.g., different name spellings) | 1.5 days | Repository |
| 2 | Export graph as image | Save relationship graph view as PNG/SVG | 1 day | Graph renderer |
| 3 | Verse density heatmap | Show which books/chapters reference an entity most frequently | 1.5 days | entity_verse_index |
| 4 | AI-assisted entity linking | Suggest new entity-verse connections based on text analysis | 3 days | FTS + entity data |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| People browser | ✅ Data ready (14,529 entities from TIPNR) | P0 | Pre-seeded by data pipeline |
| Bible places | ✅ Data ready (cross-linked with atlas) | P0 | 1,335 geocoded locations |
| Relationship graph | ✅ Data ready (4,826 relationships) | P0 | Force-directed layout planned |
| Event browser | ✅ Data ready (68 timeline events) | P0 | Cross-linked with timeline module |
| Family tree | Planned | P1 | Specialized genealogy view |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 11.5 days | Phase 1 DB |
| **Phase 6 (Data Integration)** | 2 days | Data pipeline seed DB |
| **Phase 7** | 7 days | P1/P2 features |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
| 2026-02-28 | Updated with data pipeline stats, added P1/P2 enhancements | — |
