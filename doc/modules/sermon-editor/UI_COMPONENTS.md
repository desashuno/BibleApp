# Sermon Editor — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("sermon_editor") { config -> SermonEditorPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `sermon_editor` |
| **Category** | Write |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `SermonEditorPane` | Main pane container | No |
| `SermonList` | Browse sermons with search | No |
| `SermonEditView` | Sermon title + metadata + sections | No |
| `SectionEditor` | Single section with type/title/content | No |
| `SectionTypePicker` | Choose section type | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [S] Sermon Editor               [v] [x]  |
+------------------------------------------+
| [< Back to List]                         |
+------------------------------------------+
| Title: [The Good Shepherd              ] |
| Passage: [John 10:1-18      ]            |
| Date: [2026-03-15           ]            |
+------------------------------------------+
| [Introduction] ▼                         |
|   Opening remarks about sheep and...     |
+------------------------------------------+
| [Point 1] ▼                              |
|   Title: [The Gate]                      |
|   "I am the gate for the sheep..."       |
+------------------------------------------+
| [Illustration] ▼                         |
|   Story about a Palestinian shepherd...   |
+------------------------------------------+
| [+ Add Section]                          |
+------------------------------------------+
| Auto-saved 2s ago                        |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen editor; list as separate screen |
| **Tablet** | List + editor side-by-side |
| **Desktop** | Workspace pane |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Inserts verse reference at cursor |
| User taps verse ref in sermon | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **List** | Sermon list with search | Default / back |
| **Editing** | Section editor | Sermon selected |
| **Saving** | "Saving..." indicator | Auto-save triggered |
| **Empty** | "No sermons yet" + CTA | No sermons |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Section navigation | Semantic heading levels per section |
| Auto-save status | Screen reader announces save state |
| Keyboard | Tab between sections; Ctrl+S force save |
