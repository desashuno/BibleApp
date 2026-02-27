# Resource Library — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `ResourceRepository` | CRUD + FTS5 index for resource entries | 2 days | Phase 1 DB |
| 2 | Create `ResourceLibraryComponent` | VerseBus subscription, resource filtering | 2 days | Repository |
| 3 | Create `ResourceLibraryPane` | Resource selector + entry list | 2 days | Component |
| 4 | Resource import pipeline | Parse and bulk-insert resource packages | 2 days | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Resource download manager | Download resources from online catalog | 2 days | Network layer |
| 2 | Resource versioning | Track and update resource versions | 1 day | Repository |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Resource highlighting | Highlight within resource entries | 1 day | Highlight system |
| 2 | Resource comparison | Side-by-side commentary comparison | 1.5 days | UI |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Resource library | Planned | P0 | Per-verse entries |
| Resource store | Planned (download manager) | P1 | Online catalog |
| Commentary comparison | Planned | P2 | Side-by-side |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 3** | P0 #1-4 | ~8 days | Phase 2 complete |
| **Phase 4** | P1 | ~3 days | Phase 3 |
| **Future** | P2 | ~2.5 days | Phase 4 |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | -- |
