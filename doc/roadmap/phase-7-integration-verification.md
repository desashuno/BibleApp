# Phase 7 — Verificación Integral de la Aplicación

> Comprobar la congruencia de todos los módulos, que la UI funciona correctamente,
> que el layout tipo VSCode opera sin fallos, que las Biblias en español están
> disponibles, y que ningún módulo usa datos hardcodeados o placeholders.
> **Prerequisites**: Phase 6 (Data Integration) complete; seed DB (`biblestudio-seed.db`) generated.

---

## 7.1 Congruencia de Módulos

Verificar que los 22 pane types registrados en `PaneRegistry` se interconectan
correctamente a través de VerseBus, navegación y estado compartido.

### Bible Reader
- [x] `BibleReaderPane` renderiza versículos desde la seed DB (no datos hardcodeados)
- [x] Cambio de versículo emite evento por VerseBus y todos los módulos suscritos reaccionan
- [x] Selector de traducción muestra todas las Biblias de la tabla `bibles`

### Cross-References
- [x] `CrossReferencePane` recibe `globalVerseId` de VerseBus y muestra referencias reales
- [x] Navegar a una referencia cruzada actualiza el Bible Reader vía VerseBus
- [x] `CrossRefRepositoryImpl` lee de la seed DB (verificado: usa `referenceQueries`)

### Word Study
- [x] `WordStudyPane` muestra entradas del lexicón desde `lexicon_entries`
- [x] Búsqueda FTS5 en `fts_lexicon` devuelve resultados reales
- [x] Ocurrencias de palabra leídas de `word_occurrences`

### Morphology / Interlinear
- [x] `InterlinearPane` muestra datos morfológicos reales (TAHOT/TAGNT) por versículo
- [x] `ParsingDecoder` decodifica correctamente los códigos de parsing
- [x] `ReverseInterlinearPane` muestra el texto interlineal inverso desde la seed DB

### Passage Guide
- [x] `PassageGuidePane` genera reporte agregando datos de cross-refs, morphology, lexicon
- [x] No contiene datos hardcodeados — todo proviene de queries a la seed DB

### Knowledge Graph
- [x] `KnowledgeGraphPane` renderiza entidades y relaciones desde `entities`/`relationships`
- [x] Búsqueda FTS5 en `fts_entities` funciona correctamente
- [x] Navegación de entidad a versículos asociados vía `entity_verse_index`

### Theological Atlas
- [x] `TheologicalAtlasPane` muestra mapa OSM con pins de `geographic_locations`
- [x] Auto-centrado por versículo funciona vía VerseBus + `location_verse_index`
- [x] Tile cache en disco funciona (sin descargas repetidas)

### Timeline
- [x] `TimelinePane` muestra eventos reales de `timeline_events` agrupados por era
- [x] No contiene eventos hardcodeados

### Reading Plans
- [x] `ReadingPlanPane` muestra los planes de la seed DB
- [x] `BuiltInPlans.kt` UUIDs coinciden con los de la seed DB
- [x] Progreso de lectura se persiste en `reading_plan_progress`

### Note Editor
- [x] `NoteEditorPane` crea, edita, busca y elimina notas en la DB
- [x] No contiene notas de ejemplo hardcodeadas

### Sermon Editor
- [x] `SermonEditorPane` crea, edita y gestiona sermones en la DB
- [x] No contiene sermones de ejemplo hardcodeados

### Search
- [x] `SearchPane` ejecuta búsqueda FTS5 en `fts_verses` con resultados reales
- [x] Filtros por libro/traducción funcionan correctamente

### Highlights & Bookmarks
- [x] `HighlightsPane` crea y muestra highlights persistidos en la DB
- [x] `BookmarksPane` crea y muestra bookmarks persistidos en la DB
- [x] No contienen datos de ejemplo hardcodeados

### Import/Export
- [x] `ImportExportScreen` importa/exporta datos reales (notas, highlights, bookmarks)
- [x] Formatos soportados (JSON, CSV) funcionan correctamente

### Settings
- [x] `SettingsScreen` lee y persiste configuraciones en la tabla `app_settings`
- [x] Cambio de fuente, tema y Biblia por defecto se aplican correctamente

### Dashboard
- [x] `DashboardPane` muestra estadísticas reales (versículos leídos, notas, etc.)
- [x] No contiene datos de ejemplo hardcodeados

### Resource Library & Module System
- [x] `ResourceLibraryPane` lista módulos instalados desde la DB
- [x] `ModuleManagerPane` permite instalar/desinstalar módulos

### Exegetical Guide
- [x] `ExegeticalGuidePane` agrega datos de múltiples fuentes (morphology, lexicon, cross-refs)
- [x] No contiene datos de ejemplo hardcodeados

### Audio Sync
- [x] `AudioSyncPane` estructura lista pero sin datos hardcodeados de audio
- [x] Si no hay archivos de audio, el pane muestra estado vacío (no datos fake)

### Text Comparison & Syntax Search
- [x] `TextComparisonPane` compara traducciones reales de la seed DB
- [x] `SyntaxSearchPane` busca en datos morfológicos reales

## 7.2 Verificación de UI Funcional

Comprobar que cada pane renderiza correctamente, responde a interacciones y
no muestra pantallas vacías ni errores inesperados.

- [x] Cada uno de los 22+ panes se puede abrir sin crash
- [x] Los panes muestran estados de carga (loading) mientras obtienen datos
- [x] Los panes muestran estados vacíos apropiados cuando no hay datos (no crashes)
- [x] Los panes muestran mensajes de error claros cuando falla una query
- [ ] Navegación entre panes (clic en referencia cruzada → Bible Reader) funciona
- [ ] Scroll largo en listas grandes (versículos, resultados de búsqueda) es fluido
- [ ] Tema claro y oscuro se aplican correctamente en todos los panes
- [ ] Fuentes y tamaños configurados en Settings se reflejan en todos los panes de texto

## 7.3 Layout tipo VSCode

Verificar que el sistema de layout (`LayoutNode`, splits, tabs, presets) funciona
como un IDE: paneles divididos, pestañas, redimensionado, drag & drop.

### Splits
- [x] `LayoutNode.Split` divide el workspace horizontal y verticalmente
- [x] `resizeSplit()` ajusta el ratio y se persiste tras debounce (2s)
- [x] Ratio se clampea entre 0.1 y 0.9 (no colapsa paneles)

### Tabs
- [x] `LayoutNode.Tabs` agrupa múltiples panes con pestañas
- [x] `switchTab()` cambia la pestaña activa correctamente
- [x] Cerrar una pestaña reduce el grupo; si queda 1, se convierte en Leaf

### Presets
- [x] Preset **Default**: Bible Reader solo
- [x] Preset **Study**: Reader + Cross-References + Word Study (tabs)
- [x] Preset **Exegesis**: Reader + Morphology + Passage Guide (tabs)
- [x] Preset **Writing**: Reader + Notes + Sermon Editor (tabs)
- [x] Preset **Research**: Reader + Search + Resources + Knowledge Graph (nested split)

### Persistencia
- [x] Layout se serializa a JSON y se guarda en `workspace_layouts`
- [x] Al reabrir la app, el layout se restaura exactamente como se dejó
- [x] Auto-save con debounce de 2 segundos funciona
- [x] `saveWorkspace()` fuerza guardado inmediato

### Operaciones de árbol
- [x] `addPane()` añade un pane al layout (split horizontal)
- [x] `removePane()` elimina un pane sin romper el árbol
- [x] `movePane()` mueve un pane de una posición a otra
- [x] Si el layout queda vacío, se aplica el preset Default

## 7.4 Biblias en Español

Verificar disponibilidad de las traducciones más importantes en español.
Actualmente el pipeline incluye 5 Biblias en inglés (KJV, ASV, WEB, YLT, BBE).

### Fuente: Beblia/Holy-Bible-XML-Format
> Repositorio con 1,000+ versiones en 200+ idiomas en formato XML.
> URL: https://github.com/Beblia/Holy-Bible-XML-Format.git

### Traducciones en español de dominio público / licencia libre
| Traducción | Archivo Beblia | Abreviatura | Estado |
|-----------|----------------|-------------|--------|
| Sagradas Escrituras 1569 | `Spanish1569Bible.xml` | SE1569 | Public Domain |
| Reina-Valera 1909 | `SpanishBible.xml` | RV1909 | Dominio público |
| Reina-Valera Española | `SpanishRVESBible.xml` | RVES | Public Domain |
| Versión Biblia Libre 2022 | `SpanishVBL2022Bible.xml` | VBL | CC BY-SA 4.0 |

- [x] Investigar disponibilidad de traducciones de dominio público en Beblia repo
- [x] Identificar formato XML de Beblia (`<bible><testament><book><chapter><verse>`)
- [ ] Añadir descarga de Bibles XML desde Beblia al pipeline (`download.py`)
- [ ] Crear normalizer para formato Beblia XML (`normalizers/beblia_xml.py`)
- [ ] Verificar que las traducciones en español se muestran correctamente en el Bible Reader

## 7.5 Auditoría de Datos Reales vs. Placeholders

Revisar **TODOS** los módulos. Si un módulo usa datos hardcodeados que no
provienen de la seed DB SQL, **borrarlos y dejar el módulo vacío de datos**
(estructura y UI intactas, pero sin datos fake).

### Regla general
> Si los datos no vienen de `biblestudio-seed.db` vía SQLDelight queries,
> se eliminan. El módulo mostrará su estado vacío ("No data available").

### Auditoría por módulo
- [x] **Bible Reader** — Verificar: datos de `bibles`/`verses` tables. Sin hardcodeo.
- [x] **Cross-References** — Verificar: `crossRefsFromVerse()` query. Sin stubs.
- [x] **Word Study** — Verificar: `lexicon_entries` + `word_occurrences`. Sin datos fake.
- [x] **Morphology / Interlinear** — Verificar: tabla `morphology`. Sin datos de ejemplo.
- [x] **Passage Guide** — Verificar: agrega queries reales. Sin texto de ejemplo.
- [x] **Knowledge Graph** — Verificar: `entities` + `relationships`. Sin nodos hardcodeados.
- [x] **Theological Atlas** — Verificar: `geographic_locations`. Sin coordenadas fake.
- [x] **Timeline** — Verificar: `timeline_events`. Sin eventos hardcodeados.
- [x] **Reading Plans** — Verificar: `BuiltInPlans.kt` UUIDs = seed DB UUIDs. Sin planes fake.
- [x] **Note Editor** — Verificar: CRUD vía `notes` table. Sin notas de ejemplo.
- [x] **Sermon Editor** — Verificar: CRUD vía `sermons` table. Sin sermones de ejemplo.
- [x] **Search** — Verificar: FTS5 queries. Sin resultados fake.
- [x] **Highlights** — Verificar: CRUD vía `highlights` table. Sin highlights de ejemplo.
- [x] **Bookmarks** — Verificar: CRUD vía `bookmarks` table. Sin bookmarks de ejemplo.
- [x] **Dashboard** — Verificar: estadísticas reales de la DB. Sin números fake.
- [x] **Resource Library** — Verificar: lista módulos de la DB. Sin módulos fake.
- [x] **Module System** — Verificar: gestión real de módulos. Sin datos stub.
- [x] **Import/Export** — Verificar: importa/exporta datos reales. Sin datos de ejemplo.
- [x] **Settings** — Verificar: lee/escribe `app_settings`. Sin config hardcodeada.
- [x] **Exegetical Guide** — Verificar: agrega queries reales. Sin texto de ejemplo.
- [x] **Audio Sync** — Verificar: estado vacío si no hay audio. Sin audio fake.
- [x] **Text Comparison** — Verificar: compara traducciones reales. Sin texto de ejemplo.
- [x] **Syntax Search** — Verificar: busca en morfología real. Sin resultados fake.

### Archivos sospechosos de revisar
- [ ] `PaneRegistry.init()` — Los placeholder builders (`Napier.w("Placeholder pane…")`) deben ser reemplazados por builders reales de cada feature module
- [x] `DefaultSettingsComponent.PLACEHOLDER_TIMESTAMP` — Reemplazado por `Clock.System.now()`
- [x] `DefaultWorkspaceComponent.autoSaveTimestamp` — Reemplazado por timestamp real

---

## Phase 7 Exit Criteria

- [x] Los 22+ pane types se abren sin crash y muestran datos reales de la seed DB
- [x] VerseBus propaga cambios de versículo a todos los módulos suscritos
- [x] Layout tipo VSCode: splits, tabs, presets, resize, move, persistencia — todo funcional
- [ ] Al menos 1 traducción en español (dominio público) disponible en la seed DB vía Beblia XML
- [x] Cero módulos con datos hardcodeados/placeholders (todo viene de la seed DB o está vacío)
- [ ] `PaneRegistry` placeholder builders reemplazados por builders reales
- [x] Timestamps de placeholder reemplazados por valores reales (`Clock`)
