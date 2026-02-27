# Import / Export — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | OSIS parser | Parse OSIS XML to internal entities | 3 days | — |
| 2 | USFM parser | Parse USFM to internal entities | 2 days | — |
| 3 | JSON sync export | Export annotations as sync JSON | 1.5 days | Annotation tables |
| 4 | JSON sync import | Import with LWW merge | 2 days | Sync strategy |
| 5 | Import UI | Format picker + preview + progress | 2 days | Parsers |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Sword importer | Parse Sword module format | 3 days | — |
| 2 | Export to OSIS | Serialize Bible data to OSIS XML | 2 days | Bible tables |
| 3 | PDF export | Export notes/sermons to PDF | 3 days | Writing tables |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Import OSIS/USFM | Planned | P0 |
| Sync across devices | Planned (JSON) | P0 |
| Export to PDF | P1 | P1 |
| Sword import | P1 | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 3** | 10.5 days | Phase 1 DB |
| **Phase 4** | 8 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
