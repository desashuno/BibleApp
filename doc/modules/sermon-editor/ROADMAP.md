# Sermon Editor — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Sermon schema | Create `sermons`, `sermon_sections`, `fts_sermons` tables | 1 day | Phase 1 DB |
| 2 | `SermonRepository` | CRUD + search + sections | 2 days | Schema |
| 3 | Section editor | Editable section blocks with type | 3 days | Repository |
| 4 | Auto-save | Debounced persist (1.5s) | 1 day | Editor |
| 5 | Sermon list + search | Browse sermons with FTS search | 1.5 days | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Markdown formatting | Bold, italic, headers in sections | 2 days | Editor |
| 2 | Verse ref insertion | Insert linked verse refs from VerseBus | 1.5 days | VerseBus |
| 3 | Export to PDF/Markdown | Export sermon for printing | 2 days | Import/Export |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Sermon editor | Planned | P0 |
| Section-based outline | Planned | P0 |
| Verse insertion | P1 | P1 |
| Export | P1 | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 4** | 8.5 days | Phase 1 DB |
| **Phase 5** | 5.5 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
