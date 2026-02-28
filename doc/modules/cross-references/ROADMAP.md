# Cross-References — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `CrossRefRepository` | `getReferences(verseId)` with dual-index queries | 1.5 days | Phase 1 DB |
| 2 | Load TSK data | Seed ~100K cross-references from bundled JSON | 1 day | Repository |
| 3 | Create `CrossReferenceComponent` | Subscribe to VerseBus, manage state | 2 days | Repository + VerseBus |
| 4 | Create `CrossReferencePane` | Reference list with type badges and previews | 2 days | Component |
| 5 | Reference tap → VerseBus | Publish `VerseSelected(targetVerseId)` | 0.5 days | VerseBus |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Inline expansion | Show full target verse text on tap without navigating | 1 day | Basic pane |
| 2 | Reference type filtering | Filter by parallel/quotation/allusion with chip toggles | 0.5 days | Reference types |
| 3 | Parallel passages section | Synoptic parallel group cards with multi-version comparison | 1.5 days | ParallelRepository |
| 4 | Confidence heatmap | Visual indicator showing reference strength (from OpenBible.info votes) | 1 day | confidence column |
| 5 | Cross-ref graph view | Network visualization of verse interconnections | 2 days | Cross-ref data |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | User-added references | Allow users to create custom cross-references | 1.5 days | Repository |
| 2 | Export reference chain | Copy a chain of connected verses as formatted text | 1 day | Pane |
| 3 | Related passages by topic | Suggest thematically related passages using entity overlap | 2 days | Knowledge graph |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Cross-reference panel | ✅ Data ready (344,799 pairs from OpenBible.info) | P0 | Pre-seeded by data pipeline |
| Parallel passages | ✅ Data ready (177 synoptic entries) | P1 | — |
| Bidirectional refs | ✅ Implemented (dual-index queries) | P0 | Both source→target and target→source |
| Reference confidence | ✅ Data ready (score column populated) | P0 | Unique to BibleStudio |
| Reference graph | Planned | P1 | Network visualization |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 3** | P0 #1–5 | ~7 days | Phase 2 (VerseBus) |
| **Phase 3.5** | P1 #1–5 | ~6 days | Phase 3 complete |
| **Phase 6 (Data Integration)** | Seed DB wiring | ~1 day | Data pipeline |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | — |
| 2026-02-28 | Updated with data pipeline integration, added P1/P2 enhancements | — |
