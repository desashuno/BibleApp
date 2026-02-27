# Bible Reader — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical (blockers or high priority)

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Implement `BibleRepository` | `getBooks()`, `getChapters()`, `getVerses()`, `getVerse()` with SQLDelight | 2 days | Phase 1 (DB schema) |
| 2 | Create `BibleReaderComponent` | `StateFlow<BibleReaderState>`, chapter navigation, VerseBus publish/subscribe | 3 days | Repository complete |
| 3 | Create `BibleReaderPane` composable | `LazyColumn` verse list with superscript numbers, styled text | 3 days | Component complete |
| 4 | Implement verse tap → VerseBus | Publish `LinkEvent.VerseSelected` on single tap | 0.5 days | VerseBus (Phase 2) |
| 5 | Subscribe to incoming `VerseSelected` | Scroll to verse when other pane publishes event | 0.5 days | VerseBus (Phase 2) |
| 6 | Register in PaneRegistry | `PaneRegistry.register("bible_reader")` with builder | 0.5 days | PaneRegistry (Phase 2) |

### P1 — Important (improve user experience)

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Book/chapter picker bottom sheet | Grid layout for book selection → chapter selection | 2 days | Component complete |
| 2 | Long-press verse selection | Range selection for highlight creation | 1.5 days | Highlights module |
| 3 | Verse highlight overlay | Render `AnnotatedString` spans with highlight colors | 1 day | Highlights data |
| 4 | Text Comparison — parallel view | Side-by-side columns per translation | 2 days | Multiple installed Bibles |
| 5 | Text Comparison — interleaved view | Verse-by-verse alternating translations | 1.5 days | Parallel view done |
| 6 | Text Comparison — diff highlighting | Word-level difference highlighting between translations | 2 days | Comparison views done |
| 7 | Subscribe to `PassageSelected` | Load verse range when another pane publishes passage | 0.5 days | VerseBus |

### P2 — Desirable (nice-to-have)

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Continuous chapter scrolling | Pre-load next/previous chapters as user scrolls | 2 days | Basic chapter loading |
| 2 | Reading mode toggle | Paragraph mode vs verse-by-verse mode | 1 day | HTML text support |
| 3 | Bookmark indicator per verse | Small icon on bookmarked verses | 0.5 days | Bookmarks data |
| 4 | Swipe gesture for chapter navigation | Swipe left/right on mobile to change chapters | 1 day | Mobile shell |

---

## 2. Absorbed Features

| Original feature | Origin | Integration | Status |
|-----------------|--------|-------------|--------|
| Text Comparison Tool | missing-p0/text-comparison-tool | Integrated as a reader mode with parallel and interleaved views | Pending |
| Reverse Interlinear Link | Phase 4 / reverse-interlinear | Reader renders linked English words that navigate to morphology | Pending |

---

## 3. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Multi-version comparison | Planned (parallel + interleaved views) | P1 | Word-level diff is unique to BibleStudio |
| Visual copy (formatted verse copy) | Not started | P2 | Could be added as share functionality |
| Reading history | Covered by bookmarks-history module | — | Separate module |
| Split-screen reader | Covered by workspace multi-pane | — | Workspace feature |
| Red letter text | Not started | P2 | Requires HTML markup in verse data |
| Paragraph view | Planned (reading mode toggle) | P2 | Dependent on `html_text` column |

---

## 4. Phase Estimates

| Phase | Improvements included | Total effort | Prerequisites |
|-------|----------------------|-------------|--------------|
| **Phase 3** (Core) | P0 #1–6, P1 #1 | ~12 days | Phase 2 complete (VerseBus, PaneRegistry) |
| **Phase 3.5** (Comparison) | P1 #4–6 | ~5.5 days | Phase 3 Bible Reader done |
| **Phase 4** (Integration) | P1 #2–3, #7 | ~3 days | Highlights module, Phase 3 complete |
| **Future** | P2 #1–4 | ~4.5 days | Phase 4 complete |

---

## 5. Success Metrics

| Metric | Current value | Target | How to measure |
|--------|-------------|--------|---------------|
| Chapter load time | — | < 10 ms | Benchmark test with KJV (31,102 verses) |
| FTS5 search latency | — | < 50 ms | Benchmark test with full Bible text |
| Verse tap → VerseBus latency | — | < 16 ms (1 frame) | Manual profiling with SystemTrace |
| Test coverage | 0% | ≥ 80% | jacoco/kover report |
| Accessibility audit | — | WCAG AA compliant | Manual audit with TalkBack/VoiceOver |

---

## 6. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created from Phase 3 tasks | — |
