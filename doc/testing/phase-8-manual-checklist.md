# Phase 8 — Manual Testing Checklist

> Items that require human visual/interactive verification.
> Run the app on desktop and check each item.

## Bible Reader
- [ ] Hebrew/Greek characters display correctly (no tofu/boxes)
- [ ] Chapter loading feels responsive (< 100ms perceived)
- [ ] Navigation libro → capítulo → versículo is fluid
- [ ] Long chapters scroll smoothly (no jank)

## Cross-References
- [ ] Confidence heatmap colors are readable and consistent
- [ ] Clicking a reference navigates to the correct verse

## Word Study / Lexicon
- [ ] Click on word → word study panel flow is smooth
- [ ] Hebrew/Greek original words render correctly

## Morphology / Interlinear
- [ ] Interlinear grid is legible with aligned annotations
- [ ] ParsingDecoder output is human-readable

## Knowledge Graph
- [ ] Canvas renders 200+ nodes at 60 fps (no lag)
- [ ] Zoom and pan are responsive
- [ ] Node labels are readable

## Theological Atlas
- [ ] OSM tiles load and cache correctly
- [ ] Pins are clicable with popup info
- [ ] Offline tile cache works (disconnect and reload)

## Timeline
- [ ] Horizontal scroll through events is smooth
- [ ] Events are readable at different zoom levels

## Reading Plans
- [ ] Daily view is clear with completion markers
- [ ] Progress percentage updates in real-time

## Note Editor & Sermon Editor
- [ ] Editor is fluid with no input lag
- [ ] Auto-save works (edit, wait 2s, reopen)
- [ ] Markdown renders correctly in preview

## Layout (VSCode-like)
- [ ] Splitting panes horizontally/vertically works
- [ ] Tab switching is responsive
- [ ] Resizing splits with drag works smoothly
- [ ] Layout persists after app restart
- [ ] All 5 presets produce correct layouts

## Theme & Fonts
- [ ] Light theme applies consistently to all panes
- [ ] Dark theme applies consistently to all panes
- [ ] Font size changes in Settings propagate to all text panes

## Import/Export
- [ ] Export notes → JSON and re-import (round-trip)
- [ ] Export highlights → CSV and re-import (round-trip)
- [ ] Malformed import file shows clear error message

## Dashboard
- [ ] Statistics update when notes/highlights are added
- [ ] Verse of the day rotates daily
