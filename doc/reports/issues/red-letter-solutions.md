# Red-Letter Feature — Solution Design Document

## 1. Canonical Markup Format Decision

### Problem
The UI looks for `"wj"` while parser outputs are inconsistent across sources.

### Proposed Solution
Adopt **`<wj>...</wj>`** as canonical internal format for `verses.html_text` across all ingestion paths.

### Alternatives Considered
1. Keep OSIS `<q who="Jesus">` directly
2. Keep raw USFM `\\wj ... \\wj*`
3. Normalize to `<wj>...</wj>`

### Recommendation
Use option 3 (`<wj>...</wj>`) for parser simplicity and uniform UI rendering.

---

## 2. Data Pipeline Fix — Populate `html_text`

### Problem
Normalizers always insert `NULL` in `html_text`.

### Proposed Solution
- Add optional source analysis + extraction in:
  - `data-pipeline/normalizers/bible_text.py`
  - `data-pipeline/normalizers/beblia_xml.py`
- If source red-letter tags are absent, keep `NULL` but log warning and document limitation.
- If present, normalize and write `<wj>...</wj>`.

### Risks
- Source files may not provide red-letter metadata for many versions.

---

## 3. Partial-Verse Red-Letter Rendering

### Problem
Current UI applies red at verse-level boolean granularity.

### Proposed Solution
- Create `composeApp/.../ui/util/RedLetterParser.kt`:
  - Parse `<wj>` segments from `htmlText`
  - Map segments to `plainText` index ranges
- Update `BibleReaderPane.kt` (`ParagraphView`, `VerseRow`, style resolver) to apply red only where offsets are inside ranges.

### Acceptance Criteria
- A verse with mixed speaker text renders only Jesus segments in red.

---

## 4. TextComparison Red-Letter Support

### Problem
Comparison query/model omit `html_text`.

### Proposed Solution
- Update `Bible.sq` query `versesForComparisonByGlobalId` to select `v.html_text`.
- Update repository/domain/UI so comparison entries include plain + html text.
- Reuse red-letter parser for styled rendering in `TextComparisonPane.kt`.

### Migration Note
No `.sqm` migration needed (column already exists); query/interface regeneration is enough.

---

## 5. Per-Pane vs Global Toggle Refinement

### Problem
Per-pane nullable state (`null=inherit`) toggles using `false` fallback, not effective value.

### Proposed Solution
- In `DefaultBibleReaderComponent.toggleRedLetter()`, resolve effective from settings when local is null, then invert.
- Add helper that reads global red-letter from `SettingsRepository` (key `red_letter`) and caches latest value.

---

## 6. Wire BibleModuleHandler

### Problem
`BibleModuleHandler` is currently a stub.

### Proposed Solution
- Keep this as follow-up epic unless parser/import repository wiring is available in this cycle.
- For this cycle: document current limitation explicitly in issue/fix log and avoid partial unsafe wiring.

---

## 7. Test Coverage Plan

### Unit tests
- `RedLetterParserTest.kt`:
  - null/no-tag input
  - single/multi `<wj>` ranges
  - malformed tags handled safely
  - mapping with extra HTML tags
- `DefaultBibleReaderComponentTest.kt`:
  - toggle resolves inherited global value before inversion
- `BibleRepositoryImplTest.kt`:
  - `html_text` maps into `Verse.htmlText`

### UI tests (Compose)
- `BibleReaderPaneTest.kt`:
  - red-letter enabled + `<wj>` source produces highlighted text behavior path
  - red-letter disabled does not apply red style path

---

## 8. Implementation Priority
1. Canonical `<wj>` parser utility + tests
2. BibleReader partial styling
3. Per-pane toggle refinement
4. TextComparison `html_text` propagation + UI styling
5. Focused tests for repository/component/UI
6. Pipeline normalization improvements/logging
7. BibleModuleHandler wiring as separate epic

## 9. Risks & Mitigations
- **Risk:** Source datasets lack red-letter metadata.
  - **Mitigation:** warnings + explicit docs; feature remains ready for sources that provide markup.
- **Risk:** Text mapping drift between `htmlText` and plain text.
  - **Mitigation:** parser fallback strategy + deterministic tests for edge cases.
- **Risk:** broad repo baseline issues hide regressions.
  - **Mitigation:** run targeted compile/tests for touched modules and files.
