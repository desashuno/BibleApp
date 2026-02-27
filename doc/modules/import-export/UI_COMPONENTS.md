# Import / Export — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("import_export") { config -> ImportExportPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `import_export` |
| **Category** | Tools |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `ImportExportPane` | Main pane with import/export tabs | No |
| `FormatPicker` | Import format selector (OSIS/USFM/Sword) | No |
| `ImportPreview` | Preview parsed data before confirming | No |
| `ExportScopePicker` | Select what data to export | No |
| `ProgressIndicator` | Import/export progress bar | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [<>] Import / Export            [v] [x]  |
+------------------------------------------+
| [Import]  [Export]                        |
+------------------------------------------+
|                                          |
|  Import from file:                       |
|  +------------------------------------+  |
|  | [OSIS] [USFM] [Sword] [Sync JSON] |  |
|  +------------------------------------+  |
|                                          |
|  [Choose File...]                        |
|                                          |
|  -- or --                                |
|                                          |
|  Export:                                 |
|  ( ) All annotations                    |
|  ( ) Selected notes                     |
|  ( ) Sermons                            |
|  ( ) Full backup                        |
|                                          |
|  [Export as JSON]  [Export as OSIS]       |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen with tab navigation |
| **Tablet** | Import/export side-by-side |
| **Desktop** | Workspace pane |

---

## 5. Verse Bus Interaction

N/A — Import/Export does not interact with VerseBus.

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Idle** | Format/scope selection | Default |
| **Parsing** | Spinner + file name | File selected |
| **Preview** | Parsed data table | Parsing complete |
| **Importing** | Progress bar | User confirmed |
| **Complete** | Success message + summary | Done |
| **Error** | Error details + retry | Parse/import failure |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| File picker | Native platform file dialog |
| Progress | Screen reader announces percentage |
| Format selection | Radio group with clear labels |
