# Cross-References — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("cross_references") { config ->
    CrossReferencePane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `cross_references` |
| **Builder** | `CrossReferencePane` |
| **Category** | Study |

---

## 2. Screens / Panes

### 2.1 CrossReferencePane (workspace pane)

| Aspect | Detail |
|--------|--------|
| Pane Header | "Cross-References" title, `link` icon, source verse reference |
| Toolbar | No |
| Min width | 260dp |
| Min height | 300dp |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `CrossReferencePane` | `composeApp/.../features/crossreferences/ui/CrossReferencePane.kt` | Main pane | No |
| `ReferenceItem` | `composeApp/.../features/crossreferences/ui/ReferenceItem.kt` | Single reference with type badge + preview | Yes |
| `ParallelPassageGroup` | `composeApp/.../features/crossreferences/ui/ParallelPassageGroup.kt` | Synoptic parallel group card | Yes |

---

## 4. Descriptive Wireframe

```
┌──────────────────────────────────────────┐
│ [🔗] Cross-References       [⋮] [✕]     │  ← Pane Header
│ Genesis 1:1                              │  ← Source verse
├──────────────────────────────────────────┤
│                                          │
│ ┌─ [Parallel] John 1:1 ──────────────┐  │
│ │ In the beginning was the Word...   │  │
│ │                              [▼]   │  │  ← Expandable
│ └────────────────────────────────────┘  │
│                                          │
│ ┌─ [Quotation] Hebrews 11:3 ─────────┐ │
│ │ Through faith we understand that... │ │
│ └────────────────────────────────────┘  │
│                                          │
│ ┌─ [Allusion] Isaiah 40:21 ──────────┐ │
│ │ Have ye not known? have ye not...   │ │
│ └────────────────────────────────────┘  │
│                                          │
│ 12 references                            │
└──────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen list; back button returns to reader |
| **Medium** (600–839dp) | Side panel alongside reader |
| **Expanded** (840dp+) | Workspace pane in study layout |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Loads and displays references for new verse |
| User taps reference | Publishes `VerseSelected(targetVerseId)` |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a verse to see cross-references" | No verse selected |
| **Loading** | Shimmer placeholders (4 rows) | Verse selection event received |
| **Content** | Reference list with type badges | Data loaded |
| **Empty** | "No cross-references found" | Verse has no references |
| **Error** | Error message + retry | Query failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Reference list refresh | `200ms` | `EaseOut` | Crossfade when source verse changes |
| Inline expansion | `250ms` | `EaseInOut` | AnimateContentSize when reference is expanded |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | "Cross-reference: [type] [target verse reference]" |
| Keyboard navigation | `Tab` between references; `Enter` to navigate; `Space` to expand |
| Contrast | Type badges use `paneStudy` color on `surface` (WCAG AA) |
