# Module System — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | `.bsmodule` parser | Parse ZIP manifest + validate | 2 days | — |
| 2 | Bible importer | Insert bibles/books/chapters/verses | 3 days | Phase 1 DB |
| 3 | Resource importer | Insert commentaries/dictionaries | 2 days | Phase 1 DB |
| 4 | Module browser UI | List installed modules | 1.5 days | Repository |
| 5 | Uninstall flow | Remove module data | 1 day | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Version checking | Compare installed vs manifest | 1 day | Parser |
| 2 | Auto-update | Check and apply updates | 2 days | Versions |
| 3 | Import progress | Determinate progress bar | 1 day | Importer |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Resource store | P2 (future) | P2 |
| Module manager | Planned | P0 |
| Auto-update | Planned | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 3** | 9.5 days | Phase 1 DB |
| **Phase 4** | 4 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
