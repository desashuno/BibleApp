# Resource Library — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("resource_library") { config ->
    ResourceLibraryPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `resource_library` |
| **Builder** | `ResourceLibraryPane` |
| **Category** | Resources |

---

## 2. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `ResourceLibraryPane` | `composeApp/.../features/resources/ui/ResourceLibraryPane.kt` | Main pane | No |
| `ResourceSelector` | `composeApp/.../features/resources/ui/ResourceSelector.kt` | Dropdown for resource selection | Yes |
| `EntryCard` | `composeApp/.../features/resources/ui/EntryCard.kt` | Single resource entry card | Yes |
| `ResourceImportDialog` | `composeApp/.../features/resources/ui/ResourceImportDialog.kt` | Import resource dialog | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [Lib] Resource Library — Gen 1:1 [..][X] |  <- Pane Header
+------------------------------------------+
| Resource: [Matthew Henry Commentary  v]  |  <- Resource selector
+------------------------------------------+
|                                          |
| Matthew Henry Commentary                 |
| ---                                      |
| In the beginning God created the heaven  |
| and the earth. The first verse of the    |
| Bible gives us a satisfying account...   |
|                                          |
| ---                                      |
| Gill's Exposition                        |
| ---                                      |
| In the beginning... This opening word... |
|                                          |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen; resource selector at top |
| **Medium** (600-839dp) | Side panel with scrollable entries |
| **Expanded** (840dp+) | Workspace pane alongside Bible Reader |

---

## 5. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a verse" | No verse selected |
| **Loading** | Shimmer entry cards | VerseSelected received |
| **Content** | Resource entries grouped | Data loaded |
| **Empty** | "No resources available" + import button | No entries for verse |
| **Error** | Error + retry | Query failure |

---

## 6. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Resource selector | Dropdown with semantic label "Select resource" |
| Entry content | Each entry card: "[Resource title]: [content preview]" |
| Keyboard navigation | `Tab` between entries; resource selector via `Space` |
