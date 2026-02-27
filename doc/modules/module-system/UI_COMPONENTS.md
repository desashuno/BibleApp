# Module System — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("module_system") { config -> ModuleSystemPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `module_system` |
| **Category** | Tools |

---

## 2. Screens / Panes

### 2.1 ModuleSystemPane

| Aspect | Detail |
|--------|--------|
| Pane Header | "Modules", `extension` icon |
| Toolbar | [Install] button, search field |
| Min width | 300dp |

---

## 3. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `ModuleSystemPane` | Main pane container | No |
| `ModuleCard` | Installed module card | Yes |
| `ModuleDetail` | Module info + uninstall | No |
| `InstallProgress` | Import progress bar | No |
| `ModuleBrowser` | Filterable module list | No |

---

## 4. Descriptive Wireframe

```
+------------------------------------------+
| [E] Modules                    [v] [x]   |
+------------------------------------------+
| [Search...]              [+ Install]      |
+------------------------------------------+
| +- Bible ----------------------------+   |
| | [B] King James Version    v1.0.0   |   |
| |     66 books  |  4.5 MB            |   |
| +------------------------------------+   |
| +- Lexicon ---------------------------+  |
| | [L] Strong's Hebrew    v1.0.0       |  |
| |     8,674 entries  |  1.8 MB        |  |
| +------------------------------------+   |
| 2 modules installed                      |
+------------------------------------------+
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** (0–599dp) | Full-screen list; detail as new screen |
| **Tablet** (600–899dp) | List-detail side-by-side |
| **Desktop** (900dp+) | Workspace pane; detail in dialog |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| N/A | Module system does not interact with VerseBus |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer cards | Initial load |
| **Content** | Module card list | Loaded |
| **Empty** | "No modules installed" + CTA | No modules |
| **Installing** | Progress bar | Import in progress |
| **Error** | Error message + retry | Validation failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Card appear | `200ms` | `EaseOut` | Fade + slide-up |
| Progress | Continuous | Linear | Determinate bar |
| Removal | `250ms` | `EaseInOut` | Slide-out |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | "Module: [name], [type], [version]" |
| Keyboard | Tab between modules; Enter for detail |
| Screen reader | Progress announces percentage |
