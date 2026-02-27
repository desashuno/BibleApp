# Workspace — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `WorkspaceConfig.Default` | — | Load active workspace |
| `WorkspaceConfig.Named(uuid)` | `uuid: String` | Load specific workspace |
| `WorkspaceConfig.Preset(preset)` | `preset: WorkspacePreset` | Apply preset layout |

```kotlin
sealed class WorkspaceConfig : Parcelable {
    @Parcelize data object Default : WorkspaceConfig()
    @Parcelize data class Named(val uuid: String) : WorkspaceConfig()
    @Parcelize data class Preset(val preset: WorkspacePreset) : WorkspaceConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| All pane modules | via PaneRegistry | `paneType`, `config: Map` | Rendering each Leaf |

---

## 3. Pane Opening (Workspace)

Workspace is the host, not a pane. It opens other panes via PaneRegistry.

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://workspace/{uuid}` | `biblestudio://workspace/abc-123` | Loads named workspace |
| `biblestudio://workspace/preset/{name}` | `biblestudio://workspace/preset/study` | Applies preset |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Neither** | Workspace does not participate in VerseBus |

---

## 6. Inter-Module Communication Diagram

```
+------------------+
|   RootComponent   |
+--------+---------+
         | childStack
         v
+------------------+  PaneRegistry  +----------------+
|   Workspace      | ------------> | All Pane Types  |
+------------------+               +----------------+
```
