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
| 1 | Inline expansion | Show full target verse text on tap | 1 day | Basic pane |
| 2 | Reference type filtering | Filter by parallel/quotation/allusion | 0.5 days | Reference types |
| 3 | Parallel passages section | Synoptic parallel group cards | 1.5 days | ParallelRepository |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Cross-reference panel | Planned (TSK + custom types) | P0 | TSK provides comprehensive base |
| Parallel passages | Planned (synoptic groups) | P1 | — |
| Bidirectional refs | Planned (dual-index) | P0 | Both source→target and target→source |
| Reference confidence | Planned (score column) | P0 | Unique to BibleStudio |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 3** | P0 #1–5 | ~7 days | Phase 2 (VerseBus) |
| **Phase 3.5** | P1 #1–3 | ~3 days | Phase 3 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | — |
