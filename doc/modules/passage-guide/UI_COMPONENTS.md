# Passage Guide — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("passage_guide") { config ->
    PassageGuidePane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `passage_guide` |
| **Builder** | `PassageGuidePane` |
| **Category** | Study |

---

## 2. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `PassageGuidePane` | `composeApp/.../features/passageguide/ui/PassageGuidePane.kt` | Main pane with scrollable sections | No |
| `GuideSection` | `composeApp/.../features/passageguide/ui/GuideSection.kt` | Collapsible section header + content | Yes |
| `CrossRefSection` | `composeApp/.../features/passageguide/ui/CrossRefSection.kt` | Cross-references summary | No |
| `CommentarySection` | `composeApp/.../features/passageguide/ui/CommentarySection.kt` | Commentary entries | No |
| `NotesSection` | `composeApp/.../features/passageguide/ui/NotesSection.kt` | User notes for passage | No |
| `KeyWordsSection` | `composeApp/.../features/passageguide/ui/KeyWordsSection.kt` | Key word definitions | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [Book] Passage Guide — Gen 1:1  [..] [X] |  <- Pane Header
+------------------------------------------+
| "In the beginning God created..."         |  <- Verse text
+------------------------------------------+
|                                          |
| v Cross-References (12)                  |  <- Collapsible
|   John 1:1 — In the beginning was...    |
|   Hebrews 11:3 — Through faith...       |
|   (show all)                             |
|                                          |
| v Commentary (3)                         |  <- Collapsible
|   Matthew Henry: The creation of the...  |
|   Gill: In the beginning of the...      |
|                                          |
| > Key Words (4)                          |  <- Collapsed
|                                          |
| v Notes (1)                              |  <- Collapsible
|   My note on creation...                 |
|                                          |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen scrollable report |
| **Medium** (600-839dp) | Side panel; sections stack vertically |
| **Expanded** (840dp+) | Workspace pane; optionally 2-column section layout |

---

## 5. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a verse to see the passage guide" | No verse selected |
| **Loading** | Skeleton sections (progressive) | VerseBus event received |
| **Content** | Populated collapsible sections | Report built |
| **Empty Section** | "No data" label within section | Specific source returned empty |
| **Error** | Error + retry | All queries failed |

---

## 6. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Section headings | Each `GuideSection` is `heading` role + aria-expanded |
| Keyboard navigation | `Tab` between sections; `Enter/Space` to toggle; `Tab` into section content |
| Progressive loading | Screen reader announces "Loading cross-references..." per section |
