# Timeline — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("timeline") { config -> TimelinePane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `timeline` |
| **Category** | Study |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `TimelinePane` | Main pane container | No |
| `TimelineCanvas` | Horizontally scrollable timeline | No |
| `EraBar` | Color-coded era segments | No |
| `EventCard` | Event detail popup | Yes |
| `TimelineZoomControls` | Zoom in/out/reset buttons | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [T] Timeline                    [v] [x]  |
+------------------------------------------+
| < [+] [-] [Reset]  Era: [All v]        > |
+------------------------------------------+
| |Creation |Patriarchs | Exodus |Judges | |
| |---------|-----------|--------|-------| |
| |         |           |        |       | |
| |  Adam   | Abraham   | Moses  |Samson | |
| |  Noah   | Joseph    |Joshua  |       | |
| |         |           |        |       | |
+------------------------------------------+
|  Selected: Abraham  (~2000 BCE)          |
|  "God calls Abram to leave Ur"           |
|  Gen 12:1                                |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Horizontal scroll; event cards as bottom sheet |
| **Tablet** | Full timeline with detail panel below |
| **Desktop** | Workspace pane; event detail as tooltip overlay |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Highlights events associated with that verse |
| User taps verse ref in event | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer timeline | Initial load |
| **Content** | Full timeline with events | Data loaded |
| **Detail** | Event card overlay | Event tapped |
| **Empty** | "No events in this era" | Filtered to empty era |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Timeline alt | List view alternative for screen readers |
| Event labels | "Event: [title], [year], [era]" |
| Keyboard | Arrow keys to pan; Enter for event detail |
