# Highlights — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `HighlightRepository` | CRUD with verse index | 1 day | Annotation.sq |
| 2 | Create `HighlightsComponent` | State management + VerseBus | 2 days | Repository |
| 3 | Highlight overlay in reader | Render colored spans inline | 2 days | Bible Reader |
| 4 | Color picker composable | 8-color palette | 0.5 days | Design system |
| 5 | Highlight manager pane | Browse/filter/delete highlights | 2 days | Component |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Character-level selection | Highlight partial verse text | 2 days | Overlay |
| 2 | Highlight styles | Underline and box outline options | 1 day | Overlay |
| 3 | Sync support | LWW merge for highlights | 1 day | Sync infrastructure |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Verse highlighting | Planned | P0 |
| Multiple colors | Planned (8 colors) | P0 |
| Character-level | P1 | P1 |
| Highlight styles | P1 | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 4** | 7.5 days | Phase 1 DB, Bible Reader |
| **Phase 5** | 4 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
