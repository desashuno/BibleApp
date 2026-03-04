# Phase 10 — Bible Reader Enhancement: Toolbar, Context Menu & Navigation Coordinator

> Per-pane display controls, right-click context menu with "Send to" navigation, smart pane routing, and tappable cross-reference badges.

---

## Overview

This phase adds four interconnected enhancements to the Bible Reader pane:

1. **Per-pane Reader Toolbar** — inline toggle buttons for verse numbers, red letter, paragraph mode, continuous scroll, and font size, with per-pane CompositionLocal overrides so each reader instance can have independent settings.

2. **Right-click Context Menu** — full-featured verse context menu with copy, highlight, bookmark, word-level actions, and an 11-item "Send to" navigation panel.

3. **Navigation Coordinator (OpenMode)** — smart routing logic that decides whether to focus an existing pane, open a new panel, or create a new workspace.

4. **Tappable Cross-Reference Badges** — individually annotated `[a,b,c]` markers on verse rows that navigate the reader to the target verse on tap.

---

## A. Navigation Foundation

### OpenMode enum

New enum at `shared/.../core/navigation/OpenMode.kt`:

| Mode | Behavior |
|------|----------|
| `SMART` | Focus existing pane of that type if present; otherwise split the Bible Reader to add it |
| `NEW_PANEL` | Always add a new pane to the current workspace |
| `NEW_WORKSPACE` | Create a new workspace, then add the target pane |

### Workspace methods

Two new methods on `WorkspaceComponent`:

- `containsPaneType(paneType: String): Boolean` — recursively walks the `LayoutNode` tree (Leaf, Split, Tabs) to check if a pane type exists.
- `focusPaneByType(paneType: String)` — activates all tabs along the path to the target leaf, making it visible.

### Navigation wiring

- `LocalNavigateToPane` CompositionLocal provides a `(String, OpenMode, LinkEvent) -> Unit` lambda.
- `WorkspaceCallbacks.onNavigateToPane` carries the lambda from App.kt down through WorkspaceShell.
- App.kt's `WorkspaceNavScreen` implements the routing logic using `wsComponent` and `verseBus`.

---

## B. Per-Pane Reader Toolbar

### New BibleReaderState fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `showVerseNumbers` | `Boolean?` | `null` | Per-pane override; `null` = use global setting |
| `redLetter` | `Boolean?` | `null` | Per-pane override |
| `paragraphMode` | `Boolean?` | `null` | Per-pane override |
| `fontSize` | `Int?` | `null` | Per-pane override; clamped 10..32 |
| `showReaderToolbar` | `Boolean` | `true` | Show/hide the inline toolbar |

### Toggle methods on BibleReaderComponent

- `toggleShowVerseNumbers()` — flips `showVerseNumbers` (default `true`)
- `toggleRedLetter()` — flips `redLetter` (default `false`)
- `toggleParagraphMode()` — flips `paragraphMode` (default `false`)
- `adjustFontSize(delta: Int)` — adjusts `fontSize` by delta, clamping to 10..32
- `toggleReaderToolbar()` — toggles `showReaderToolbar`

### CompositionLocal overrides

In `PaneContent.kt`, the `"bible-reader"` case wraps the pane in `CompositionLocalProvider` that selectively overrides `LocalShowVerseNumbers`, `LocalRedLetter`, `LocalParagraphMode`, and `LocalAppFontSize` when the per-pane value is non-null.

### ReaderToolbar composable

Compact icon-button `Row` displayed between the selection/word popups and the verse content. Buttons:

| Icon | Toggle | Active tint |
|------|--------|-------------|
| FormatListNumbered | Verse numbers | `primary` |
| "Red" label | Red letter | `error` |
| ViewHeadline | Paragraph mode | `primary` |
| UnfoldMore | Continuous scroll | `primary` |
| TextDecrease | Font size -1 | `onSurface` |
| TextIncrease | Font size +1 | `onSurface` |

A Tune icon in the pane header toolbar toggles the reader toolbar visibility.

---

## C. Right-Click Context Menu

### VerseContextMenu structure

1. **Header** — formatted verse reference (e.g., "Genesis 1:1 (KJV)")
2. **Copy Verse** — copies verse text + reference to clipboard
3. **Highlight** — submenu with color swatches from `AppColors.highlights`
4. **Bookmark** — bookmarks the verse
5. *(If word selected)* **Search "word"** / **Study "word"**
6. **Send to...** section with 11 targets:
   - Cross References, Word Study, Passage Guide, Text Comparison
   - Interlinear, Morphology, Exegetical Guide
   - Timeline, Atlas, Knowledge Graph, Commentary
7. **Open in New Panel** — `OpenMode.NEW_PANEL`
8. **Open in New Workspace** — `OpenMode.NEW_WORKSPACE`

### Right-click detection

`VerseRow` accepts an `onRightClick` callback. A `Modifier.pointerInput` detects `PointerEventType.Press` with `buttons.isSecondaryPressed`, consumes the event, and invokes the callback.

---

## D. Tappable Cross-Reference Badges

### Annotation approach

Each cross-reference letter (`a`, `b`, `c`, ...) in the `[a,b,c]` badge gets its own `addStringAnnotation("XREF", targetVerseId)`. This enables per-badge tap detection.

### Tap handling

In `ClickableText`'s `onClick`, XREF annotations are checked **before** WORD annotations:

```kotlin
annotatedText.getStringAnnotations("XREF", offset, offset)
    .firstOrNull()?.let { /* navigate reader */ }
```

### Navigation

`onCrossReferenceTapped(targetVerseId: Long)` publishes `LinkEvent.VerseSelected` to `VerseBus`, navigating the Bible Reader to the target verse.

---

## Files Changed

| File | Action |
|------|--------|
| `shared/.../core/navigation/OpenMode.kt` | New |
| `shared/.../workspace/component/WorkspaceComponent.kt` | Add 2 methods |
| `shared/.../workspace/component/DefaultWorkspaceComponent.kt` | Promote focusPaneByType + add containsPaneType |
| `composeApp/.../ui/workspace/NavigationLocals.kt` | New |
| `composeApp/.../ui/workspace/WorkspaceCallbacks.kt` | Add onNavigateToPane |
| `composeApp/.../ui/workspace/WorkspaceShell.kt` | CompositionLocalProvider |
| `composeApp/.../App.kt` | Wire navigation logic |
| `shared/.../bible_reader/component/BibleReaderComponent.kt` | Expand state + 6 methods |
| `shared/.../bible_reader/component/DefaultBibleReaderComponent.kt` | Implement toggles + xref |
| `composeApp/.../ui/workspace/PaneContent.kt` | Per-pane locals + toolbar toggle |
| `composeApp/.../ui/panes/BibleReaderPane.kt` | Toolbar, context menu, xref badges |
| `shared/.../bible_reader/component/DefaultBibleReaderComponentTest.kt` | New tests |
| `shared/.../workspace/component/DefaultWorkspaceComponentTest.kt` | New tests |
| `composeApp/.../ui/panes/BibleReaderPaneTest.kt` | Augmented tests |
| `doc/roadmap/phase-10-bible-reader-enhancement.md` | New |
| `doc/roadmap/README.md` | Add Phase 10 entry |
