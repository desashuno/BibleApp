# Morphology / Interlinear — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `MorphologyRepository` | `getMorphology(verseId)` ordered by word position | 1.5 days | Phase 1 DB |
| 2 | Implement `ParsingDecoder` | `V-AAI-3S` → human-readable parts-of-speech | 2 days | Morphology codes spec |
| 3 | Create `InterlinearComponent` | Subscribe VerseBus, manage display modes | 2 days | Repository |
| 4 | Create `InterlinearPane` | 4-row word grid composable | 3 days | Component |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Hebrew RTL text rendering | Proper bidirectional layout for Hebrew words | 1 day | Word grid |
| 2 | Greek polytonic diacritics | Correct rendering of polytonic Greek | 0.5 days | Word grid |
| 3 | Word tap → StrongsSelected | Publish to VerseBus for word study navigation | 0.5 days | VerseBus |
| 4 | Reverse Interlinear sub-feature | Map English words back to original language | 3 days | Morphology data + alignment |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Inline tooltip mode | Show morphology as popovers on word hover | 1.5 days | Display modes |
| 2 | Copy morphology data | Export word analysis to clipboard | 0.5 days | — |

---

## 2. Absorbed Features

| Original feature | Origin | Integration | Status |
|-----------------|--------|-------------|--------|
| Reverse Interlinear | Phase 4 §4.5 | Integrated as display mode mapping English → original | Pending |

---

## 3. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Interlinear panel | Planned | P0 | 4-row word grid |
| Reverse interlinear | Planned | P1 | English-to-original mapping |
| Inline morphology | Planned (Inline mode) | P2 | Tooltip on hover |
| Parsing filter | Not started | P2 | Filter words by POS |

---

## 4. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 4** | P0 #1–4, P1 #1–3 | ~10.5 days | Phase 3 complete |
| **Phase 4.5** | P1 #4, P2 | ~5 days | Phase 4 complete |

---

## 5. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 4 tasks | — |
