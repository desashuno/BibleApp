# Theological Atlas — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Location schema + seed data | Create table + populate biblical locations | 2 days | Phase 1 DB |
| 2 | `AtlasRepository` | Query locations by verse, name | 1 day | Schema |
| 3 | Map renderer | Compose Canvas map with bundled tiles | 4 days | — |
| 4 | Markers + clustering | Location markers with zoom clustering | 2 days | Map |
| 5 | Location detail | Info + verse refs on marker tap | 1 day | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Journey routes | Draw travel routes (Paul's journeys, Exodus) | 3 days | Map |
| 2 | High-res tiles | Download on demand | 2 days | Map |
| 3 | Cross-link knowledge graph | Locations ↔ entities | 1 day | Knowledge graph |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Bible atlas | Planned | P0 |
| Location markers | Planned | P0 |
| Journey routes | P1 | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 10 days | Phase 1 DB |
| **Phase 6** | 6 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
