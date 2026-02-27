# Bookmarks & History — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `BookmarkRepository` | CRUD with folder support | 1.5 days | Annotation.sq |
| 2 | Create `BookmarksComponent` | State + VerseBus + history | 2 days | Repository |
| 3 | Bookmarks pane | Folder list + bookmark items | 2 days | Component |
| 4 | History tracking | Auto-record verse navigation | 1 day | VerseBus |
| 5 | Quick bookmark | One-tap bookmark from reader | 0.5 days | Component |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Folder reordering | Drag-and-drop folder/bookmark sort | 1.5 days | Pane |
| 2 | Sync support | LWW merge for bookmarks | 1 day | Sync infra |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Bookmarks with folders | Planned | P0 |
| Navigation history | Planned | P0 |
| Bookmark labels | Planned | P0 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 4** | 7 days | Phase 1 DB |
| **Phase 5** | 2.5 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
