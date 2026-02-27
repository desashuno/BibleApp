# Search — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("search") { config ->
    SearchPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `search` |
| **Builder** | `SearchPane` |
| **Category** | Tool |

---

## 2. Screens / Panes

### 2.1 SearchPane (workspace pane)

| Aspect | Detail |
|--------|--------|
| Pane Header | "Search" title, `search` icon, scope filter chip row |
| Toolbar | Yes — Search text field with clear button |
| Min width | 300dp |
| Min height | 400dp |

### 2.2 SearchContent (full-screen on mobile)

| Aspect | Detail |
|--------|--------|
| Top App Bar | Yes — Integrated search field |
| FAB | No |
| Bottom Sheet | No |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `SearchPane` | `composeApp/.../features/search/ui/SearchPane.kt` | Main search pane | No |
| `SearchResultItem` | `composeApp/.../features/search/ui/SearchResultItem.kt` | Single result with snippet + highlighted matches | Yes |
| `SearchFilterBar` | `composeApp/.../features/search/ui/SearchFilterBar.kt` | Scope/book/testament filter chips | Yes |
| `SearchHistoryDropdown` | `composeApp/.../features/search/ui/SearchHistoryDropdown.kt` | Recent searches dropdown | No |
| `SyntaxSearchPane` | `composeApp/.../features/search/ui/SyntaxSearchPane.kt` | Morphology-aware query builder | No |

---

## 4. Descriptive Wireframe

```
┌──────────────────────────────────────────┐
│ [🔍] Search                    [⋮] [✕]  │  ← Pane Header
├──────────────────────────────────────────┤
│ [🔍 Search the Bible...            [✕]] │  ← Search field
│ [All] [Bible] [Notes] [Resources]       │  ← Scope filter chips
├──────────────────────────────────────────┤
│                                          │
│ Genesis 1:1 — KJV                        │
│ In the beginning **God created** the     │
│ heavens and the earth.                   │
│                                          │
│ John 1:3 — KJV                           │
│ All things were made by him; and without │
│ him was not any thing made that was...   │
│                                          │
│ (42 results)                             │
│                                          │
├──────────────────────────────────────────┤
│ 42 results · 23ms                        │  ← Status bar
└──────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen search with result list; bottom navigation hides during typing |
| **Medium** (600–839dp) | Sidebar search, results fill content area |
| **Expanded** (840dp+) | Workspace pane; can sit alongside Bible Reader |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| User taps search result | Publishes `SearchResult(globalVerseId)` → Bible Reader scrolls to verse |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Empty** | Search prompt + illustration | No query entered |
| **History** | Recent searches dropdown | Search field focused with empty query |
| **Searching** | Inline progress indicator | Query debounce executing |
| **Results** | Ranked result list with snippets | Search complete |
| **No Results** | "No results" message + suggestions | Search returned empty |
| **Error** | Error message + retry | FTS5 query failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Result list appear | `200ms` | `EaseOut` | Staggered fade-in of result items |
| Filter chip select | `150ms` | `EaseInOut` | Color transition on active chip |
| History dropdown | `200ms` | `EaseOut` | Slide down from search field |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | Each result: "Search result: [Book Chapter:Verse] [snippet]" |
| Keyboard navigation | `Ctrl+F` opens search pane; `Enter` executes; `↑/↓` navigates results |
| Contrast | Match highlight uses `tertiary` on `surface` (WCAG AA) |
| Text scaling | Result snippets respect font size setting |
