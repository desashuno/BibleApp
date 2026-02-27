# Import / Export — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  ImportExportPane                                 |
|  FormatPicker / ExportProgress / ImportPreview    |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultImportExportComponent (Decompose)         |
|  +-- Manages StateFlow<ImportExportState>         |
|  +-- Delegates to format-specific parsers         |
|  OsisParser / UsfmParser / SwordParser            |
+---------------------------------------------------+
|                      DATA                         |
|  DataExporter / DataImporter                      |
|  +-- BibleQueries / AnnotationQueries             |
|  +-- kotlinx.serialization (JSON export)          |
|  +-- File system (file read/write)                |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Import

1. **User selects file** — File picker with OSIS/USFM/Sword filter.
2. **Format detection** — File extension and header analysis.
3. **Parsing** — Format-specific parser converts to internal entities.
4. **Preview** — User reviews parsed data before confirming import.
5. **Insert** — Data inserted into SQLite within transaction.

### 2.2 Secondary Flows

- **Export** — User selects data scope and format → serialized to file.
- **Sync export** — Generates JSON envelope for device-to-device sync.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Description |
|-----------|-------|-------------|
| `Bible.sq` | `insertBible` / `insertVerse` | Bible import |
| `Annotation.sq` | `allNotes` / `allHighlights` / `allBookmarks` | Annotation export |
| `Writing.sq` | `allSermons` | Sermon export |

---

## 4. Dependency Injection

```kotlin
val importExportModule = module {
    singleOf(::OsisParser)
    singleOf(::UsfmParser)
    singleOf(::SwordParser)
    singleOf(::DataExporter)
    singleOf(::DataImporter)
    factory { (ctx: ComponentContext) ->
        DefaultImportExportComponent(ctx, get(), get(), get(), get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Strategy | Format parsers | Pluggable import per format |
| Pipeline | Import flow | Validate → parse → preview → insert |
| Transaction | DB writes | Atomicity |

---

## 6. Performance Considerations

- **OSIS import** — Streaming XML parser avoids loading entire file into memory.
- **Batch inserts** — 1000 rows per batch within transaction.
- **Export** — Streaming JSON writer for large datasets.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| OSIS + USFM + Sword | Proprietary formats | Open standards; broad compatibility |
| JSON sync export | Binary | Human-readable; debuggable; kotlinx.serialization |
| Preview before import | Direct import | User validation prevents bad data |
