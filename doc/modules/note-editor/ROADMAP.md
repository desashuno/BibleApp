# Note Editor — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `NoteRepository` | CRUD operations with FTS5 sync triggers | 1.5 days | Phase 1 DB |
| 2 | Create `NoteEditorComponent` | VerseBus subscription, auto-save with 2s debounce | 2 days | Repository |
| 3 | Create `RichTextEditor` composable | Markdown-like editor with formatting toolbar | 3 days | Compose text input |
| 4 | Create `NoteEditorPane` | Note list + editor layout | 2 days | Component + Editor |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Note tags/categories | Taggable notes for organization | 1.5 days | Schema change |
| 2 | Note linking | Cross-link notes to other notes or verses | 1 day | UUID references |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Note export | Export notes as Markdown/PDF | 1 day | import-export |
| 2 | Note templates | Pre-built note templates (sermon notes, study notes) | 1 day | UI |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Note editor | Planned | P0 | Rich text with verse linking |
| Note tags | Planned | P1 | Tag-based organization |
| Note linking | Planned | P1 | Cross-note references |
| Note export | Planned | P2 | Markdown + PDF |

---

## 3. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 3** | P0 #1-4 | ~8.5 days | Phase 2 complete |
| **Phase 4** | P1 #1-2 | ~2.5 days | Phase 3 |
| **Future** | P2 | ~2 days | Phase 4 |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | -- |
