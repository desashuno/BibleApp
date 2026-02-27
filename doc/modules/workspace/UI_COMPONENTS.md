# Workspace — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

The workspace module is the **host** that renders all other panes. It is not registered as a pane type.

| Field | Value |
|-------|-------|
| **Type key** | N/A (root shell) |
| **Builder** | `WorkspaceShell` |
| **Category** | Tools |

---

## 2. Screens / Panes

### 2.1 WorkspaceShell (root composable)

| Aspect | Detail |
|--------|--------|
| Activity Bar | Left-side vertical bar with pane type icons |
| Content Area | Recursive `LayoutNodeRenderer` |
| Min width | 360dp (mobile) |

---

## 3. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `WorkspaceShell` | `composeApp/.../workspace/ui/WorkspaceShell.kt` | Root shell | No |
| `LayoutNodeRenderer` | `composeApp/.../workspace/ui/LayoutNodeRenderer.kt` | Recursive tree renderer | No |
| `PaneContainer` | `composeApp/.../workspace/ui/PaneContainer.kt` | Header + close wrapper | Yes |
| `SplitPane` | `composeApp/.../workspace/ui/SplitPane.kt` | Draggable split divider | Yes |
| `TabGroupPane` | `composeApp/.../workspace/ui/TabGroupPane.kt` | Tab row with content | Yes |
| `WorkspaceSwitcher` | `composeApp/.../workspace/ui/WorkspaceSwitcher.kt` | Workspace dropdown | No |
| `PresetPicker` | `composeApp/.../workspace/ui/PresetPicker.kt` | Quickstart layout grid | No |
| `ActivityBar` | `composeApp/.../workspace/ui/ActivityBar.kt` | Vertical icon list | No |

---

## 4. Descriptive Wireframe

```
+------+----------------------------------------------+
|      | [Workspace: Study]      [Switch]   [+ Add]   |
|  A   +---------------------+------------------------+
|  c   |                     | [Cross-Refs] [Word] [M]|
|  t   |                     +------------------------+
|  i   |   Bible Reader      |  Cross-References      |
|  v   |   (Leaf pane)       |  (Active tab content)  |
|  i   |                     |                        |
|  t   |                     |                        |
|  y   +--[=== drag ===]-----+                        |
|  B   |                     |                        |
|  a   |                     |                        |
|  r   |                     |                        |
+------+---------------------+------------------------+
```

---

## 5. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** (0–599dp) | Single-pane stack; activity bar hidden; bottom nav |
| **Tablet** (600–899dp) | Two-pane split; simplified activity bar |
| **Desktop** (900dp+) | Full multi-pane with activity bar, split/tabs |

---

## 6. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| N/A | Workspace does not subscribe; individual panes handle VerseBus |

---

## 7. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Skeleton layout | Workspace loading from DB |
| **Empty** | Preset picker | First launch |
| **Active** | Full layout | Workspace loaded |
| **Error** | Error banner + fallback pane | Deserialization failure |

---

## 8. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Pane add/remove | `300ms` | `EaseInOut` | AnimatedVisibility |
| Tab switch | `200ms` | `EaseOut` | Crossfade |
| Workspace switch | `250ms` | `EaseInOut` | Fade transition |

---

## 9. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic descriptions | Activity bar: "Open [pane] panel" |
| Keyboard navigation | `Ctrl+1..9` switch tabs; `Ctrl+W` close pane |
| Split resize | Arrow keys adjust ratio when focused |
