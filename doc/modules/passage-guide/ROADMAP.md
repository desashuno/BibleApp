# Passage Guide — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Create `PassageGuideComponent` | Orchestrate 6 parallel repository queries | 3 days | All source repositories |
| 2 | Create `PassageGuidePane` | Scrollable collapsible sections | 2 days | Component |
| 3 | Progressive section rendering | Show skeleton per section until data arrives | 1 day | UI |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Key words extraction | Identify and highlight important words from morphology | 2 days | MorphologyRepository |
| 2 | Section reordering | User can drag-reorder sections | 1.5 days | UI |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Passage range support | Select chapter range, not just single verse | 2 days | Component |
| 2 | Export passage report | PDF/Markdown export of full report | 1.5 days | import-export |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Passage Guide panel | Planned | P0 | Aggregated study hub |
| Section customization | Planned (reordering) | P1 | Logos allows show/hide |
| Exegetical Guide | Separate module (exegetical-guide) | — | BibleStudio separates concerns |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 4** | P0 #1-3 | ~6 days | Phase 3 modules |
| **Phase 4.5** | P1 #1-2 | ~3.5 days | Phase 4 |
| **Future** | P2 | ~3.5 days | Phase 4.5 |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 4 tasks | -- |
