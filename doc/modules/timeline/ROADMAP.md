# Timeline — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Timeline schema + seed data | Create table + populate biblical events | 3 days | Phase 1 DB |
| 2 | `TimelineRepository` | Query events by era, verse, range | 1.5 days | Schema |
| 3 | Timeline canvas | Horizontal scrolling Compose Canvas | 3 days | — |
| 4 | Era bar | Color-coded era segments | 1 day | Canvas |
| 5 | Event detail card | Tap event for detail + verse link | 1 day | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Zoom levels | Overview / era / year zoom | 2 days | Canvas |
| 2 | Search events | FTS5 on event title/description | 1 day | FTS5 |
| 3 | Cross-link knowledge graph | Events ↔ entities navigation | 1 day | Knowledge graph |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Interactive timeline | Planned | P0 |
| Era navigation | Planned | P0 |
| Verse links | Planned | P0 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 9.5 days | Phase 1 DB |
| **Phase 6** | 4 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
