# Word Study — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("word_study") { config ->
    WordStudyPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `word_study` |
| **Builder** | `WordStudyPane` |
| **Category** | Study |

---

## 2. Screens / Panes

### 2.1 WordStudyPane (workspace pane)

| Aspect | Detail |
|--------|--------|
| Pane Header | Strong's number + original word, `text_fields` icon |
| Toolbar | No |
| Min width | 280dp |
| Min height | 400dp |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `WordStudyPane` | `composeApp/.../features/wordstudy/ui/WordStudyPane.kt` | Main pane container | No |
| `LexiconCard` | `composeApp/.../features/wordstudy/ui/LexiconCard.kt` | Definition card with original word, transliteration, definition | Yes |
| `OccurrenceList` | `composeApp/.../features/wordstudy/ui/OccurrenceList.kt` | Verse occurrence list | Yes |
| `FrequencyChart` | `composeApp/.../features/wordstudy/ui/FrequencyChart.kt` | Bar chart of occurrences by book | Yes |

---

## 4. Descriptive Wireframe

```
┌──────────────────────────────────────────┐
│ [Aa] H1254 — בָּרָא            [⋮] [✕]   │  ← Pane Header
├──────────────────────────────────────────┤
│ ┌────────────────────────────────────┐   │
│ │ בָּרָא  (bara')                     │   │
│ │ Definition: to create, shape, form │   │
│ │ Usage: 54 occurrences              │   │
│ └────────────────────────────────────┘   │
│                                          │
│ ┌── Frequency by Book ───────────────┐   │
│ │ Gen ████████  (11)                 │   │
│ │ Isa ██████████████  (20)           │   │
│ │ Psa ████  (6)                      │   │
│ │ ...                                │   │
│ └────────────────────────────────────┘   │
│                                          │
│ ── Occurrences ──────────────────────    │
│ Genesis 1:1 — In the beginning God...   │
│ Genesis 1:21 — And God created great... │
│ Genesis 1:27 — So God created man...    │
│ ...                                      │
└──────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen; back button returns to previous pane |
| **Medium** (600–839dp) | Side panel in study layout |
| **Expanded** (840dp+) | Workspace pane alongside Bible Reader |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `StrongsSelected` | Loads and displays lexicon entry + occurrences |
| User taps occurrence | Publishes `VerseSelected(globalVerseId)` |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a word to study" | No Strong's selected |
| **Loading** | Spinner in definition card area | StrongsSelected received |
| **Content** | Definition card + chart + occurrence list | Data loaded |
| **Error** | Error message + retry | Query failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Definition card appear | `250ms` | `EaseOut` | Fade + slide from top |
| Chart bars animate | `400ms` | `EaseInOut` | Bars grow from zero to target width |
| Occurrence list | `200ms` | `EaseOut` | Staggered fade-in |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | "Word study: [original word], [transliteration], [definition]" |
| Keyboard navigation | `Tab` between sections; `Enter` on occurrence to navigate |
| Contrast | Original Hebrew/Greek text uses `onSurface` (WCAG AA) |
| Text scaling | All text respects font size setting |
