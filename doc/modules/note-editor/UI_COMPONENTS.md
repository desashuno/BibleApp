# Note Editor — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("note_editor") { config ->
    NoteEditorPane(config = config)
}
```

| Field | Value |
|-------|-------|
| **Type key** | `note_editor` |
| **Builder** | `NoteEditorPane` |
| **Category** | Writing |

---

## 2. Key Composables

| Composable | File | Description | Reusable |
|------------|------|-------------|----------|
| `NoteEditorPane` | `composeApp/.../features/notes/ui/NoteEditorPane.kt` | Main pane with note list + editor | No |
| `RichTextEditor` | `composeApp/.../features/notes/ui/RichTextEditor.kt` | Markdown-like rich text input | Yes |
| `NoteListPanel` | `composeApp/.../features/notes/ui/NoteListPanel.kt` | List of notes for verse | No |
| `FormattingToolbar` | `composeApp/.../features/notes/ui/FormattingToolbar.kt` | Bold/italic/heading/list toolbar | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [Edit] Note Editor — Gen 1:1  [..] [X]   |  <- Pane Header
+------------------------------------------+
| Notes for Genesis 1:1        [+]         |  <- Note list header
| > My note on creation *                  |  <- Active note (dirty)
|   Theological observations               |
+------------------------------------------+
| [B] [I] [H1] [H2] [UL] [OL]            |  <- Formatting toolbar
+------------------------------------------+
|                                          |
| In the beginning, God created...         |
|                                          |
| This verse establishes that God is the   |
| Creator of all things. The Hebrew word   |
| **bara** (H1254) is used exclusively...  |
|                                          |
| - God as Creator                         |
| - Ex nihilo creation                     |
|                                          |
+------------------------------------------+
| Auto-saved 2s ago                        |  <- Status bar
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Compact** (< 600dp) | Full-screen editor; note list as bottom sheet |
| **Medium** (600-839dp) | Side-by-side list + editor |
| **Expanded** (840dp+) | Workspace pane; full editor with toolbar |

---

## 5. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Waiting** | "Select a verse to see notes" | No verse selected |
| **Empty** | "No notes yet" + create button | Verse has no notes |
| **List** | Note list for verse | Notes loaded |
| **Editing** | Rich text editor with toolbar | Note selected |
| **Saving** | "Saving..." indicator | Auto-save in progress |

---

## 6. Animations & Transitions

| Transition | Duration | Easing | Description |
|-----------|---------|--------|-------------|
| Note switch | `200ms` | `EaseOut` | Crossfade editor content |
| Save indicator | `300ms` | `EaseInOut` | Fade in/out "Saving..." |
| Toolbar appear | `150ms` | `EaseOut` | Slide down on focus |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Editor role | `TextField` with `contentDescription` including note title |
| Formatting toolbar | Each button has semantic label ("Bold", "Italic", etc.) |
| Save status | Live region announces "Note saved" on auto-save |
| Keyboard shortcuts | `Ctrl+B` bold, `Ctrl+I` italic, `Ctrl+S` force save |
