# Morphology / Interlinear — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("morphology") { config ->
    InterlinearPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `morphology` |
| **Builder** | `InterlinearPane` |
| **Category** | Study |

---

## 2. Screens / Panes

### 2.1 InterlinearPane (workspace pane)

| Aspect | Detail |
|--------|--------|
| Pane Header | Verse reference, `translate` icon, display mode toggle |
| Toolbar | Yes — display mode chip row (Interlinear / Parallel / Inline) |
| Min width | 320dp |
| Min height | 300dp |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `InterlinearPane` | `composeApp/.../features/morphology/ui/InterlinearPane.kt` | Main pane container | No |
| `WordGrid` | `composeApp/.../features/morphology/ui/WordGrid.kt` | 4-row interlinear word grid | Yes |
| `WordCell` | `composeApp/.../features/morphology/ui/WordCell.kt` | Single word: original, transliteration, gloss, parsing | Yes |
| `ParsingBadge` | `composeApp/.../features/morphology/ui/ParsingBadge.kt` | Decoded parsing label | Yes |

---

## 4. Descriptive Wireframe

```
┌──────────────────────────────────────────┐
│ [🌐] Interlinear — Gen 1:1    [⋮] [✕]   │  ← Pane Header
├──────────────────────────────────────────┤
│ [Interlinear] [Parallel] [Inline]        │  ← Display mode
├──────────────────────────────────────────┤
│  בְּרֵאשִׁית    בָּרָא      אֱלֹהִים        │  ← Original
│  bᵉrēʾšîṯ   bārāʾ    ʾĕlōhîm         │  ← Transliteration
│  In beginning created   God             │  ← Gloss
│  N-fsc       V-Qal-3ms N-mpc           │  ← Parsing
│                                          │
│  אֵת       הַשָּׁמַיִם     וְאֵת     הָאָרֶץ │
│  ʾēṯ      haššāmayim  wᵉʾēṯ   hāʾāreṣ │
│  [obj]     the heavens and[obj] the earth│
│  Acc       N-mpc       Conj+Acc N-fsa   │
└──────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen; horizontal scroll for word grid |
| **Medium** (600–839dp) | Side panel; word grid wraps |
| **Expanded** (840dp+) | Workspace pane; full interlinear grid |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Loads interlinear data for new verse |
| User taps word | Publishes `StrongsSelected(strongsNumber)` |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a verse" message | No verse selected |
| **Loading** | Shimmer grid placeholder | VerseSelected received |
| **Content** | Interlinear word grid | Data loaded |
| **Error** | Error message + retry | Query failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Word grid appear | `250ms` | `EaseOut` | Fade in word cells |
| Mode switch | `300ms` | `EaseInOut` | Layout animation between interlinear/parallel/inline |
| Word tap highlight | `150ms` | `EaseOut` | Background flash on tapped word |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | Each word cell: "[original] transliterated as [transliteration], meaning [gloss], parsed as [full parsing]" |
| RTL support | Hebrew text uses `TextDirection.Rtl`; Greek uses `TextDirection.Ltr` |
| Keyboard navigation | Arrow keys to navigate word grid; Enter to open word study |
| Contrast | Original text on `surface` background meets WCAG AA |
