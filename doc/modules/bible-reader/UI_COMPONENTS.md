# Bible Reader — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("bible_reader") { config ->
    BibleReaderPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `bible_reader` |
| **Builder** | `BibleReaderPane` |
| **Category** | Text |

---

## 2. Screens / Panes

### 2.1 BibleReaderPane (workspace pane)

| Aspect | Detail |
|--------|--------|
| Pane Header | Book name + chapter number, `menu_book` icon, overflow menu (comparison toggle, font size) |
| Toolbar | Yes — Book/chapter picker button, previous/next chapter arrows |
| Min width | 280dp |
| Min height | 400dp |

### 2.2 BibleReaderContent (full-screen on mobile)

| Aspect | Detail |
|--------|--------|
| Top App Bar | Yes — Book + chapter title, back arrow, overflow menu |
| FAB | No |
| Bottom Sheet | Yes — Book/chapter picker (BookChapterPicker composable) |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `BibleReaderPane` | `composeApp/.../features/biblereader/ui/BibleReaderPane.kt` | Main pane container with header and toolbar | No |
| `BibleReaderContent` | `composeApp/.../features/biblereader/ui/BibleReaderContent.kt` | Verse list content (used in both pane and full-screen) | Yes |
| `VerseItem` | `composeApp/.../features/biblereader/ui/VerseItem.kt` | Single verse row: superscript number + text + highlight overlay | Yes |
| `BookChapterPicker` | `composeApp/.../features/biblereader/ui/BookChapterPicker.kt` | Bottom sheet with book grid + chapter grid | Yes |
| `TextComparisonPane` | `composeApp/.../features/biblereader/ui/TextComparisonPane.kt` | Parallel/interleaved multi-version view | No |
| `VersionSelector` | `composeApp/.../features/biblereader/ui/VersionSelector.kt` | Dropdown chip list for selecting Bible versions to compare | Yes |

---

## 4. Descriptive Wireframe

```
┌──────────────────────────────────────────┐
│ [📖] Genesis 1            [≡] [⋮] [✕]   │  ← Pane Header (sage accent)
├──────────────────────────────────────────┤
│ [◀ Prev] [Book/Chapter Picker] [Next ▶] │  ← Toolbar
├──────────────────────────────────────────┤
│                                          │
│  ¹ In the beginning God created the      │
│  heavens and the earth.                  │
│                                          │
│  ² And the earth was without form, and   │
│  void; and darkness was upon the face    │
│  of the deep.                            │
│                                          │
│  ³ And God said, Let there be light:     │
│  ██ and there was light. ██              │  ← Highlight overlay
│                                          │
│  ⁴ And God saw the light, that it was    │
│  good: and God divided the light from    │
│  the darkness.                           │
│              ...                         │
│                                          │
├──────────────────────────────────────────┤
│ KJV · Genesis 1  (31 verses)            │  ← Status bar
└──────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen reader, bottom sheet for book picker, swipe left/right for chapter navigation |
| **Medium** (600–839dp) | Sidebar + content layout; book picker in sidebar panel |
| **Expanded** (840–1199dp) | Workspace pane (can be split with one other pane) |
| **Large** (≥ 1200dp) | Multi-pane workspace; reader typically occupies primary (left) pane |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` from VerseBus | `LazyColumn.animateScrollToItem()` to target verse; highlight with accent ring |
| Receives `PassageSelected` from VerseBus | Load passage range, scroll to first verse in range |
| Receives `SearchResult` from VerseBus | Navigate to chapter containing result verse, scroll and highlight |
| User taps verse | Publishes `VerseSelected` to VerseBus; updates `selectedVerseId` in state |
| User long-presses verse | Enters range selection mode; publishes range on confirmation |
| User taps Strong's link | Publishes `StrongsSelected` to VerseBus |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer placeholder (8 verse-height rows) | Chapter change or initial load |
| **Empty** | "No content available" message + "Install Bible Module" CTA | No Bible installed |
| **Error** | Error message + retry button | Database or I/O failure |
| **Content** | Verse list with formatted text, highlights, verse numbers | Data loaded successfully |
| **Comparison** | Side-by-side or interleaved multi-version view | User toggles comparison mode |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Chapter change | `300ms` | `EaseInOut` | Crossfade between old and new chapter content |
| Verse selection highlight | `200ms` | `EaseOut` | Background color animation on selected verse |
| Scroll-to-verse | `400ms` | `EaseInOut` | `animateScrollToItem()` to target verse position |
| Book picker open/close | `250ms` | `EaseOut` | Bottom sheet slide up/down |
| Comparison mode toggle | `300ms` | `EaseInOut` | Layout animation between single and split view |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | Each `VerseItem` has `Modifier.semantics { contentDescription = "Verse $number: $text" }` |
| Keyboard navigation | `Ctrl+↑/↓` for verse-by-verse navigation, `Ctrl+←/→` for chapter navigation |
| Contrast | Verse number uses `primary` color on `surface` (WCAG AA verified) |
| Text scaling | Respects user's font size setting (12–28sp); `LazyColumn` adjusts item heights |
| Screen reader | Book/chapter picker announces "Book: Genesis, Chapter: 1" on focus |
