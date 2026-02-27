# Word Study — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `WordStudyRepository` | Strong's lookup, occurrences, related words | 2 days | Phase 1 DB |
| 2 | Create `WordStudyComponent` | Subscribe VerseBus `StrongsSelected`, manage state | 2 days | Repository |
| 3 | Create `WordStudyPane` | Definition card + occurrence list | 2 days | Component |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Frequency chart | Bar chart of occurrences grouped by book | 2 days | Occurrence data |
| 2 | Semantic domain grouping | Group occurrences by semantic category | 1.5 days | Lexicon data |
| 3 | Related words section | Show words sharing same root | 1 day | Lexicon data |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Lexicon search | FTS5 search across definitions | 1 day | fts_lexicon |
| 2 | Strong's number quick-nav | Type a Strong's number directly | 0.5 days | Component |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Word study panel | Planned | P0 | Core feature |
| Frequency chart | Planned | P1 | Bar chart by book |
| Semantic domains | Planned | P1 | Domain grouping |
| Reverse interlinear link | Covered by morphology module | — | — |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 4** | P0 #1–3, P1 #1 | ~8 days | Phase 3 complete |
| **Phase 4.5** | P1 #2–3, P2 | ~3.5 days | Phase 4 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 4 tasks | — |
