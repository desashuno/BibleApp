# Exegetical Guide — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("exegetical_guide") { config -> ExegeticalGuidePane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `exegetical_guide` |
| **Category** | Study |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `ExegeticalGuidePane` | Main pane with scrollable sections | No |
| `MorphologySection` | Word-by-word parsing display | No |
| `WordStudySection` | Key vocabulary cards | No |
| `CrossRefSection` | Related passage list | No |
| `CommentarySection` | Commentary excerpts | No |
| `ContextSection` | Surrounding verses | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [E] Exegetical Guide            [v] [x]  |
+------------------------------------------+
| John 3:16                                |
| "For God so loved the world..."          |
+------------------------------------------+
| MORPHOLOGY                               |
| agapao [G25] verb.aor.act.ind.3s         |
| kosmos [G2889] noun.acc.sg.masc          |
+------------------------------------------+
| WORD STUDIES                              |
| agapao — "to love unconditionally"       |
| kosmos — "world, ordered system"         |
+------------------------------------------+
| CROSS-REFERENCES                         |
| Rom 5:8, 1 John 4:9, Eph 2:4            |
+------------------------------------------+
| COMMENTARY                               |
| "This verse encapsulates the gospel..."  |
+------------------------------------------+
| CONTEXT (John 3:14-18)                   |
| 14 "And as Moses lifted up..." ...       |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen scrollable; collapsible sections |
| **Tablet** | Side panel alongside reader |
| **Desktop** | Workspace pane; all sections expanded |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Loads full exegetical data for verse |
| User taps cross-ref | Publishes `VerseSelected(globalVerseId)` |
| User taps Strong's number | Publishes `StrongsSelected(strongsNumber)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **No verse** | "Select a verse to begin" | No VerseBus event yet |
| **Loading** | Shimmer sections | VerseBus event received |
| **Content** | All sections populated | Data loaded |
| **Partial** | Available sections shown | Some data missing (no commentary, etc.) |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Section headers | Semantic headings (H2) for each section |
| Morphology codes | Expanded descriptions for screen readers |
| Cross-refs | "Cross-reference: [verse label]" |
