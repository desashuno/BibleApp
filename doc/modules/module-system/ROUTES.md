# Module System — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `ModuleSystemConfig.Main` | — | Module browser |
| `ModuleSystemConfig.Detail(id)` | `id: String` | Module detail |

```kotlin
sealed class ModuleSystemConfig : Parcelable {
    @Parcelize data object Main : ModuleSystemConfig()
    @Parcelize data class Detail(val moduleId: String) : ModuleSystemConfig()
}
```

---

## 2. Consumed Configurations

None — Module system is self-contained.

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("module_system")
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://modules` | — | Opens module browser |
| `biblestudio://modules/{id}` | `biblestudio://modules/kjv` | Module detail |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Neither** | No VerseBus participation |

---

## 6. Inter-Module Communication Diagram

```
[.bsmodule file] --> [Module System] --> [Bible/Resource/Lexicon tables]
```
