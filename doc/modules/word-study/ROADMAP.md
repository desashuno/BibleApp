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
| 1 | Lexicon search | FTS5 search across definitions via fts_lexicon (19,570 entries indexed) | 1 day | fts_lexicon |
| 2 | Strong's number quick-nav | Type a Strong's number directly to jump to definition | 0.5 days | Component |
| 3 | Word cloud | Visual cloud of most frequent words in a book/chapter | 2 days | word_occurrences |
| 4 | Comparative usage | Show how different translations render the same Strong's word | 1.5 days | Multiple Bible texts |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Word study panel | ✅ Data ready (19,570 lexicon entries: 11,682 Hebrew + 11,035 Greek from STEPBible) | P0 | Pre-seeded by data pipeline |
| Frequency chart | ✅ Data ready (427,503 word occurrences) | P1 | Bar chart by book |
| Semantic domains | Planned | P1 | Domain grouping |
| Reverse interlinear link | Covered by morphology module | — | — |
| Word cloud | Planned | P2 | Visual frequency display |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 4** | P0 #1–3, P1 #1 | ~8 days | Phase 3 complete |
| **Phase 4.5** | P1 #2–3, P2 | ~5 days | Phase 4 complete |
| **Phase 6 (Data Integration)** | Seed DB wiring | ~0.5 days | Data pipeline |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 4 tasks | — |
| 2026-02-28 | Updated with data pipeline stats, added P2 enhancements | — |
