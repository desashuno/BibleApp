# Highlights — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("highlights") { config -> HighlightsPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `highlights` |
| **Category** | Write |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `HighlightsPane` | Main pane — highlight list/manager | No |
| `HighlightOverlay` | Inline color overlay in Bible Reader | Yes |
| `ColorPicker` | 8-color palette selector | Yes |
| `HighlightItem` | Single highlight row with verse ref + color | Yes |
| `HighlightFilter` | Filter by color/book/date | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [H] Highlights                  [v] [x]  |
+------------------------------------------+
| [All Colors v]  [Book filter v]          |
+------------------------------------------+
| [==] Gen 1:1 "In the beginning..."       |
|      Yellow  |  Feb 15, 2026             |
+------------------------------------------+
| [==] Ps 23:1 "The Lord is my..."         |
|      Blue    |  Feb 20, 2026             |
+------------------------------------------+
| [==] John 3:16 "For God so loved..."     |
|      Green   |  Feb 25, 2026             |
+------------------------------------------+
| 3 highlights                             |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen list; inline overlay in reader |
| **Tablet** | Side panel alongside reader |
| **Desktop** | Workspace pane |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Scrolls to highlights for that verse |
| User taps highlight | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer rows | Initial load |
| **Content** | Highlight list | Data loaded |
| **Empty** | "No highlights yet" + illustration | No highlights |
| **Error** | Error message + retry | Query failure |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Color names | Each color has a text label, not just visual swatch |
| Keyboard | Tab between highlights; Enter to navigate |
