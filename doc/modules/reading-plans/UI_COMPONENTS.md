# Reading Plans — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("reading_plans") { config -> ReadingPlansPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `reading_plans` |
| **Category** | Read |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `ReadingPlansPane` | Main pane container | No |
| `PlanBrowser` | List of available plans | No |
| `PlanDetail` | Plan overview + progress calendar | No |
| `DailyReadingView` | Today's passages + completion toggle | No |
| `ProgressCalendar` | Calendar heatmap of reading streaks | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [R] Reading Plans               [v] [x]  |
+------------------------------------------+
| Active: Read the Bible in 1 Year         |
| Day 45 of 365  |  12% complete           |
+------------------------------------------+
| Today's Reading (Day 45):                |
| +------------------------------------+   |
| | [>] Exodus 14  ..................  |   |
| | [>] Psalm 45   ..................  |   |
| | [>] Matt 15    ..................  |   |
| +------------------------------------+   |
| [Mark Today Complete]                    |
+------------------------------------------+
| Feb 2026                                 |
| Mo Tu We Th Fr Sa Su                     |
| [#][#][#][#][#][#][ ]                    |
| [#][#][ ][#][#][#][ ]                    |
| [#][#][#][#][ ][ ][ ]                    |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen; daily view as default |
| **Tablet** | Daily view + calendar side-by-side |
| **Desktop** | Workspace pane |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| User taps passage | Publishes `PassageSelected(startId, endId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **No active plan** | Plan browser | First use |
| **Active plan** | Daily reading + calendar | Plan activated |
| **Complete** | Congratulations + restart option | All days done |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Calendar | Each day labeled "Day [N]: [complete/incomplete]" |
| Passages | "Passage: [label], tap to read" |
| Progress | Screen reader announces percentage |
