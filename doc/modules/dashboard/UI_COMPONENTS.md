# Dashboard — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("dashboard") { config -> DashboardPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `dashboard` |
| **Category** | Tools |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `DashboardPane` | Main pane with widget grid | No |
| `ReadingPlanWidget` | Today's reading + progress bar | No |
| `RecentNotesWidget` | Last 5 edited notes | No |
| `BookmarkWidget` | Recent/pinned bookmarks | No |
| `QuickNavWidget` | Recent workspaces | No |
| `VerseOfTheDayWidget` | Daily verse display | No |
| `DashboardWidgetCard` | Shared card wrapper | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [D] Dashboard                   [v] [x]  |
+------------------------------------------+
| +-- Verse of the Day -----------+        |
| | "For God so loved..." Jn 3:16 |        |
| +-------------------------------+        |
|                                          |
| +-- Reading Plan --+  +-- Recent Notes - |
| | Day 45/365  12%  |  | My study notes   |
| | [>] Ex 14        |  | John 3 outline   |
| | [>] Ps 45        |  | Romans study     |
| +------------------+  +------------------+
|                                          |
| +-- Bookmarks -----+  +-- Workspaces --- |
| | Gen 1:1           |  | Default         |
| | John 3:16         |  | Sunday prep     |
| | Rom 8:28          |  +------------------+
| +------------------+                     |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Single-column stacked widgets |
| **Tablet** | 2-column widget grid |
| **Desktop** | 3-column widget grid in workspace pane |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| User taps verse of the day | Publishes `VerseSelected(globalVerseId)` |
| User taps reading passage | Publishes `PassageSelected(startId, endId)` |
| User taps bookmark | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer widget placeholders | Initial load |
| **Content** | Populated widgets | Data loaded |
| **Empty widget** | Widget with "Get started" CTA | No data for widget |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Widget labels | Semantic group labels for each widget |
| Verse of the day | Full verse text readable by screen reader |
| Navigation | Tab between widgets; Enter to interact |
