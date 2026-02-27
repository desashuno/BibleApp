# Import / Export — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultImportExportComponent` | Scoped | Import/export orchestration |

---

## 2. ImportExportComponent

### 2.1 Interface

```kotlin
interface ImportExportComponent {
    val state: StateFlow<ImportExportState>
    fun onSelectImportFile(fileBytes: ByteArray, fileName: String)
    fun onConfirmImport()
    fun onCancelImport()
    fun onExport(scope: ExportScope, format: ExportFormat)
}
```

### 2.2 State

```kotlin
data class ImportExportState(
    val activeTab: Tab = Tab.Import,
    val importJob: ImportJob? = null,
    val exportJob: ExportJob? = null,
    val error: AppError? = null,
)
enum class Tab { Import, Export }
```

### 2.3 State Transitions

```
Idle --> Parsing --> Preview --> Importing --> Complete
                |                          +-> Error
                +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onSelectImportFile` | File parsing | Async format detection + parse |
| `onConfirmImport` | DB transaction | Bulk insert parsed data |
| `onExport` | DB read + file write | Query data + serialize to file |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Parent | Decompose | Lifecycle |
| `BibleRepository` | → Writes | Koin DI | Bible import |
| `AnnotationQueries` | → Reads/Writes | Koin DI | Annotation export/sync import |

---

## 5. Component Registration (Koin)

```kotlin
val importExportModule = module {
    factory<ImportExportComponent> { (ctx: ComponentContext) ->
        DefaultImportExportComponent(ctx, get(), get(), get(), get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `OSIS import parses and inserts correctly`() = runTest {
    val component = DefaultImportExportComponent(TestComponentContext(), FakeImporter(), OsisParser(), ...)
    component.onSelectImportFile(osisXmlBytes, "kjv.osis.xml")
    component.state.test {
        val preview = awaitItem()
        assertThat(preview.importJob?.status).isEqualTo(ImportStatus.Previewing)
    }
}
```
