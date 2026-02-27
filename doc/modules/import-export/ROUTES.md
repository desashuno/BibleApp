# Import / Export — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `ImportExportConfig.Main` | — | Default import/export view |
| `ImportExportConfig.Import(format)` | `format: ImportFormat` | Pre-selected import format |
| `ImportExportConfig.Export(scope)` | `scope: ExportScope` | Pre-selected export scope |

---

## 2. Consumed Configurations

None — self-contained module.

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("import_export")
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://import` | — | Opens import tab |
| `biblestudio://export` | — | Opens export tab |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Neither** | No VerseBus participation |

---

## 6. Inter-Module Communication Diagram

```
[OSIS/USFM/Sword file] --> [Import/Export] --> [Bible/Annotation tables]
[Bible/Annotation tables] --> [Import/Export] --> [JSON/OSIS file]
```
