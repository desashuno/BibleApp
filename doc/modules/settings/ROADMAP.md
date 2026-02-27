# Settings — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `SettingsRepository` | Key-value CRUD with `observeSettings()` Flow | 1.5 days | Phase 1 DB |
| 2 | Create `SettingsComponent` | Typed AppSettings state management | 1.5 days | Repository |
| 3 | Create `SettingsPane` | Grouped settings UI with sections | 2 days | Component |
| 4 | Theme integration | Apply theme from settings to Compose `MaterialTheme` | 1 day | Design System |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Font size preview | Live preview of text size while adjusting slider | 0.5 days | UI |
| 2 | Locale switching | Hot-swap language without restart | 1 day | i18n system |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Settings import/export | Backup/restore settings | 0.5 days | import-export |
| 2 | Per-workspace settings | Override global settings per workspace | 1 day | Workspace module |

---

## 2. Phase Estimates

| Phase | Improvements | Total effort | Prerequisites |
|-------|-------------|-------------|--------------|
| **Phase 2** | P0 #1-4 | ~6 days | Phase 1 DB |
| **Phase 3** | P1 #1-2 | ~1.5 days | Phase 2 |
| **Future** | P2 | ~1.5 days | Phase 3 |

---

## 3. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 2 tasks | -- |
