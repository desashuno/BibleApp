# {Module Name} — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

<!-- How this module is registered in the global pane catalog. -->

```kotlin
// PaneRegistry.register("{module}") { config ->
//     {Module}Pane(config = config)
// }
```

| Field | Value |
|-------|-------|
| **Type key** | `{module}` |
| **Builder** | `{Module}Pane` |
| **Category** | {Read / Study / Write / Tools / Resources / Media} |

---

## 2. Screens / Panes

### 2.1 {Module}Pane (workspace pane)

<!-- Description of the composable when rendered as a pane in the multi-pane workspace. -->

| Aspect | Detail |
|--------|--------|
| Pane Header | {Title, icon, header actions} |
| Toolbar | {Yes/No} — {buttons/actions} |
| Min width | {N}dp |
| Min height | {N}dp |

### 2.2 {Module}Content (full-screen on mobile)

<!-- Description of the full-screen layout on mobile. -->

| Aspect | Detail |
|--------|--------|
| Top App Bar | {Yes/No} — {description} |
| FAB | {Yes/No} — {action} |
| Bottom Sheet | {Yes/No} — {content} |

---

## 3. Key Composables

<!-- List of composable functions that make up this module's UI. -->

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `{Module}Pane` | `composeApp/.../features/{module}/ui/{Module}Pane.kt` | Main pane container | No |
| `{Composable1}` | `composeApp/.../features/{module}/ui/{Composable1}.kt` | {description} | {Yes/No} |
| `{Composable2}` | `composeApp/.../features/{module}/ui/{Composable2}.kt` | {description} | {Yes/No} |

---

## 4. Descriptive Wireframe

<!-- Text-based wireframe of the main UI layout. -->

```
┌─────────────────────────────────────────┐
│ [Icon] {Pane Title}          [⋮] [✕]   │  ← Pane Header
├─────────────────────────────────────────┤
│ [Toolbar: contextual actions]           │  ← Toolbar (optional)
├─────────────────────────────────────────┤
│                                         │
│         Main content area               │
│         of the module                   │
│                                         │
│                                         │
├─────────────────────────────────────────┤
│ [Status / Footer info]                  │  ← Footer (optional)
└─────────────────────────────────────────┘
```

---

## 5. Responsive Behavior

<!-- How the UI adapts to different screen sizes. -->

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** (0–599dp) | {Full-screen navigation, simplified layout, etc.} |
| **Tablet** (600–899dp) | {Side panel, adapted layout, etc.} |
| **Desktop** (900dp+) | {Workspace pane, multi-pane, etc.} |

---

## 6. Verse Bus Interaction

<!-- How the module reacts to verse changes in other panes. -->

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` from VerseBus | {Scroll to verse, highlight, load data, etc.} |
| User selects a verse | {Publishes `VerseSelected` to VerseBus} |

---

## 7. UI States

<!-- Different visual states of the module. -->

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | {Skeleton / Spinner / Shimmer} | Initial load or data change |
| **Empty** | {Message + illustration + CTA} | No data to display |
| **Error** | {Error message + retry button} | Load failure |
| **Content** | {Main UI with data} | Data loaded successfully |
| **Searching** | {Active input + progressive results} | User searches within module |

---

## 8. Animations & Transitions

<!-- Module-specific animations. -->

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| {Panel entry} | `200ms` | `EaseOut` | {description} |
| {Content change} | `150ms` | `EaseInOut` | {description} |

---

## 9. Accessibility

<!-- Module-specific accessibility considerations. -->

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | {Composables that require `Modifier.semantics`} |
| Keyboard navigation | {Shortcuts, tab order} |
| Contrast | {Verify against DESIGN_SYSTEM §12} |
| Text scaling | {Respects user's text scale preference} |
