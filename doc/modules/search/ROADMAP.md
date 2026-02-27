# Search — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `SearchRepository` with FTS5 | `searchAll()`, `searchVerses()` using FTS5 MATCH + BM25 | 2 days | Phase 1 DB + FTS tables |
| 2 | FTS5 query builder | Support `AND`, `OR`, `NOT`, `NEAR`, prefix matching | 1.5 days | Repository |
| 3 | Create `SearchComponent` with debounce | `StateFlow<SearchState>`, 300ms debounce, result tap → VerseBus | 2 days | Repository |
| 4 | Create `SearchPane` composable | Search bar + `LazyColumn` results with highlighted snippets | 2 days | Component |
| 5 | Search history | Store last 20 queries in `search_history` table | 1 day | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Book range / testament filter | Narrow results by book range or OT/NT | 1 day | Basic search |
| 2 | Syntax search parser | Tokenizer → AST → SQL for `[LEMMA:H1234]` grammar | 3 days | Morphology data |
| 3 | Syntax search UI | `SyntaxSearchPane` with syntax help popover | 2 days | Parser |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Cross-module search tabs | Separate tabs for Bible / Notes / Resources results | 1.5 days | Basic search |
| 2 | Search suggestions | Auto-complete from history + popular queries | 1 day | History |

---

## 2. Absorbed Features

| Original feature | Origin | Integration | Status |
|-----------------|--------|-------------|--------|
| Syntax Search | Phase 3 §3.6 | Integrated as sub-pane with custom grammar parser | Pending |

---

## 3. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Basic Bible search | Planned (FTS5) | P0 | BM25 ranking equivalent |
| Morphology search | Planned (Syntax Search) | P1 | Custom grammar parser |
| Bible Word Study search | Planned via word-study module integration | P1 | — |
| Saved searches | Not started | P2 | Could store as named queries |

---

## 4. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 3** | P0 #1–5, P1 #1 | ~9.5 days | Phase 2 complete |
| **Phase 3.5** | P1 #2–3 | ~5 days | Morphology data available |
| **Future** | P2 #1–2 | ~2.5 days | Phase 3 complete |

---

## 5. Success Metrics

| Metric | Current | Target | How to measure |
|--------|---------|--------|---------------|
| FTS5 search latency | — | < 50 ms | Benchmark with full KJV text |
| Debounce effectiveness | — | ≥ 70% query reduction | Compare keystrokes vs executed queries |
| Test coverage | 0% | ≥ 80% | kover report |

---

## 6. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | — |
