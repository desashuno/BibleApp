# Workspace — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Layout persistence | Save/restore layouts to SQLite | 2 days | Settings.sq |
| 2 | LayoutNode renderer | Recursive composable for Split/Tabs/Leaf | 3 days | PaneRegistry |
| 3 | PaneContainer | Header, close button, drag handle | 1.5 days | Renderer |
| 4 | Split divider | Draggable resize | 2 days | SplitPane |
| 5 | Workspace switcher | Create, rename, delete, switch | 1.5 days | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Quickstart layouts | Preset picker on first launch | 1 day | Workspace |
| 2 | Tab groups | Merge panes into tabs | 2 days | PaneContainer |
| 3 | Pane drag & drop | Reorder/move panes | 3 days | Tab groups |
| 4 | Mobile nav stack | Single-pane mode with back nav | 2 days | Responsive shell |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Workspace export/import | Share layouts as JSON | 1 day | Serialization |
| 2 | Floating panes | Pop-out windows (desktop) | 3 days | Platform-specific |

---

## 2. Absorbed Features

| Original feature | Origin | Integration | Status |
|-----------------|--------|-------------|--------|
| Quickstart Layouts | missing-p0/quickstart-layouts | Preset picker | Pending |

---

## 3. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Multi-pane workspace | Planned | P0 | Split + Tabs |
| Workspace presets | Planned | P1 | 5 presets |
| Tab groups | Planned | P1 | — |
| Floating panels | P2 | P2 | Desktop-only |

---

## 4. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 2** | P0 #1-5 | 10 days | Phase 1 DB |
| **Phase 3** | P1 #1-4 | 8 days | Phase 2 |
| **Phase 5** | P2 #1-2 | 4 days | Phase 3 |

---

## 5. Success Metrics

| Metric | Current | Target | How to measure |
|--------|---------|--------|---------------|
| Layout load time | N/A | < 5 ms | Benchmark |
| Save reliability | N/A | 100% | Integration test |
| Preset adoption | N/A | > 80% | Analytics |

---

## 6. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
