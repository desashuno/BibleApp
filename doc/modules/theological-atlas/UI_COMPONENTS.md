# Theological Atlas — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("theological_atlas") { config -> TheologicalAtlasPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `theological_atlas` |
| **Category** | Study |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `TheologicalAtlasPane` | Main pane container | No |
| `MapView` | Compose Canvas map with tiles + markers | No |
| `LocationDetail` | Location info + verse refs | No |
| `LocationList` | Alphabetical location list | No |
| `MapZoomControls` | Zoom in/out/reset | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [M] Theological Atlas           [v] [x]  |
+------------------------------------------+
| [Map]  [List]    [Search...]              |
+------------------------------------------+
|                                          |
|    +--+  Nazareth                        |
|   /    \     * Capernaum                 |
|  | Sea  |                                |
|  | of   |  * Tiberias                    |
|   \  G  /                                |
|    +--+                                  |
|                 * Jerusalem               |
|            * Bethlehem                    |
|                                          |
| [+] [-] [Reset]                          |
+------------------------------------------+
| Selected: Jerusalem                      |
| Modern: Al-Quds  | 31.7683, 35.2137      |
| Gen 14:18, 2 Sam 5:6, Matt 21:1         |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen map; detail as bottom sheet |
| **Tablet** | Map + detail panel below |
| **Desktop** | Workspace pane; detail as tooltip overlay |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Highlights locations mentioned in verse |
| User taps verse ref | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Placeholder map | Initial load |
| **Content** | Map with markers | Data loaded |
| **Detail** | Location info overlay | Marker tapped |
| **List** | Alphabetical list | Tab switch |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Map alt | List view as accessible alternative |
| Location labels | "Location: [name], coordinates [lat, lon]" |
| Keyboard | Tab between markers; Enter for detail |
