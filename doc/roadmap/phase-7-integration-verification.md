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
- [ ] `BibleReaderPane` renderiza versículos desde la seed DB (no datos hardcodeados)
- [ ] Cambio de versículo emite evento por VerseBus y todos los módulos suscritos reaccionan
- [ ] Selector de traducción muestra todas las Biblias de la tabla `bibles`

### Cross-References
- [ ] `CrossReferencePane` recibe `globalVerseId` de VerseBus y muestra referencias reales
- [ ] Navegar a una referencia cruzada actualiza el Bible Reader vía VerseBus
- [ ] `CrossRefRepositoryImpl` lee de la seed DB (verificado: usa `referenceQueries`)

### Word Study
- [ ] `WordStudyPane` muestra entradas del lexicón desde `lexicon_entries`
- [ ] Búsqueda FTS5 en `fts_lexicon` devuelve resultados reales
- [ ] Ocurrencias de palabra leídas de `word_occurrences`

### Morphology / Interlinear
- [ ] `InterlinearPane` muestra datos morfológicos reales (TAHOT/TAGNT) por versículo
- [ ] `ParsingDecoder` decodifica correctamente los códigos de parsing
- [ ] `ReverseInterlinearPane` muestra el texto interlineal inverso desde la seed DB

### Passage Guide
- [ ] `PassageGuidePane` genera reporte agregando datos de cross-refs, morphology, lexicon
- [ ] No contiene datos hardcodeados — todo proviene de queries a la seed DB

### Knowledge Graph
- [ ] `KnowledgeGraphPane` renderiza entidades y relaciones desde `entities`/`relationships`
- [ ] Búsqueda FTS5 en `fts_entities` funciona correctamente
- [ ] Navegación de entidad a versículos asociados vía `entity_verse_index`

### Theological Atlas
- [ ] `TheologicalAtlasPane` muestra mapa OSM con pins de `geographic_locations`
- [ ] Auto-centrado por versículo funciona vía VerseBus + `location_verse_index`
- [ ] Tile cache en disco funciona (sin descargas repetidas)

### Timeline
- [ ] `TimelinePane` muestra eventos reales de `timeline_events` agrupados por era
- [ ] No contiene eventos hardcodeados

### Reading Plans
- [ ] `ReadingPlanPane` muestra los planes de la seed DB
- [ ] `BuiltInPlans.kt` UUIDs coinciden con los de la seed DB
- [ ] Progreso de lectura se persiste en `reading_plan_progress`

### Note Editor
- [ ] `NoteEditorPane` crea, edita, busca y elimina notas en la DB
- [ ] No contiene notas de ejemplo hardcodeadas

### Sermon Editor
- [ ] `SermonEditorPane` crea, edita y gestiona sermones en la DB
- [ ] No contiene sermones de ejemplo hardcodeados

### Search
- [ ] `SearchPane` ejecuta búsqueda FTS5 en `fts_verses` con resultados reales
- [ ] Filtros por libro/traducción funcionan correctamente

### Highlights & Bookmarks
- [ ] `HighlightsPane` crea y muestra highlights persistidos en la DB
- [ ] `BookmarksPane` crea y muestra bookmarks persistidos en la DB
- [ ] No contienen datos de ejemplo hardcodeados

### Import/Export
- [ ] `ImportExportScreen` importa/exporta datos reales (notas, highlights, bookmarks)
- [ ] Formatos soportados (JSON, CSV) funcionan correctamente

### Settings
- [ ] `SettingsScreen` lee y persiste configuraciones en la tabla `app_settings`
- [ ] Cambio de fuente, tema y Biblia por defecto se aplican correctamente

### Dashboard
- [ ] `DashboardPane` muestra estadísticas reales (versículos leídos, notas, etc.)
- [ ] No contiene datos de ejemplo hardcodeados

### Resource Library & Module System
- [ ] `ResourceLibraryPane` lista módulos instalados desde la DB
- [ ] `ModuleManagerPane` permite instalar/desinstalar módulos

### Exegetical Guide
- [ ] `ExegeticalGuidePane` agrega datos de múltiples fuentes (morphology, lexicon, cross-refs)
- [ ] No contiene datos de ejemplo hardcodeados

### Audio Sync
- [ ] `AudioSyncPane` estructura lista pero sin datos hardcodeados de audio
- [ ] Si no hay archivos de audio, el pane muestra estado vacío (no datos fake)

### Text Comparison & Syntax Search
- [ ] `TextComparisonPane` compara traducciones reales de la seed DB
- [ ] `SyntaxSearchPane` busca en datos morfológicos reales

## 7.2 Verificación de UI Funcional

Comprobar que cada pane renderiza correctamente, responde a interacciones y
no muestra pantallas vacías ni errores inesperados.

- [ ] Cada uno de los 22+ panes se puede abrir sin crash
- [ ] Los panes muestran estados de carga (loading) mientras obtienen datos
- [ ] Los panes muestran estados vacíos apropiados cuando no hay datos (no crashes)
- [ ] Los panes muestran mensajes de error claros cuando falla una query
- [ ] Navegación entre panes (clic en referencia cruzada → Bible Reader) funciona
- [ ] Scroll largo en listas grandes (versículos, resultados de búsqueda) es fluido
- [ ] Tema claro y oscuro se aplican correctamente en todos los panes
- [ ] Fuentes y tamaños configurados en Settings se reflejan en todos los panes de texto

## 7.3 Layout tipo VSCode

Verificar que el sistema de layout (`LayoutNode`, splits, tabs, presets) funciona
como un IDE: paneles divididos, pestañas, redimensionado, drag & drop.

### Splits
- [ ] `LayoutNode.Split` divide el workspace horizontal y verticalmente
- [ ] `resizeSplit()` ajusta el ratio y se persiste tras debounce (2s)
- [ ] Ratio se clampea entre 0.1 y 0.9 (no colapsa paneles)

### Tabs
- [ ] `LayoutNode.Tabs` agrupa múltiples panes con pestañas
- [ ] `switchTab()` cambia la pestaña activa correctamente
- [ ] Cerrar una pestaña reduce el grupo; si queda 1, se convierte en Leaf

### Presets
- [ ] Preset **Default**: Bible Reader solo
- [ ] Preset **Study**: Reader + Cross-References + Word Study (tabs)
- [ ] Preset **Exegesis**: Reader + Morphology + Passage Guide (tabs)
- [ ] Preset **Writing**: Reader + Notes + Sermon Editor (tabs)
- [ ] Preset **Research**: Reader + Search + Resources + Knowledge Graph (nested split)

### Persistencia
- [ ] Layout se serializa a JSON y se guarda en `workspace_layouts`
- [ ] Al reabrir la app, el layout se restaura exactamente como se dejó
- [ ] Auto-save con debounce de 2 segundos funciona
- [ ] `saveWorkspace()` fuerza guardado inmediato

### Operaciones de árbol
- [ ] `addPane()` añade un pane al layout (split horizontal)
- [ ] `removePane()` elimina un pane sin romper el árbol
- [ ] `movePane()` mueve un pane de una posición a otra
- [ ] Si el layout queda vacío, se aplica el preset Default

## 7.4 Biblias en Español

Verificar disponibilidad de las traducciones más importantes en español.
Actualmente el pipeline incluye 5 Biblias en inglés (KJV, ASV, WEB, YLT, BBE).

### Traducciones con licencia libre (objetivo inmediato)
- [ ] Investigar disponibilidad de Reina-Valera 1909 (dominio público) en fuentes open-source
- [ ] Investigar Sagradas Escrituras 1569 (dominio público)
- [ ] Investigar Versión Moderna 1893 de H.B. Pratt (dominio público)
- [ ] Añadir las traducciones de dominio público al pipeline (`download.py` + `bible_text.py`)
- [ ] Verificar que las traducciones en español se muestran correctamente en el Bible Reader

### Traducciones con copyright (requieren licencia)
> Estas traducciones son las más usadas en el mundo hispano pero tienen copyright.
> Se documentan aquí para futuro contacto con las editoriales.

| Traducción | Abreviatura | Editorial | Estado |
|-----------|-------------|-----------|--------|
| Biblia Textual | BTX | Sociedad Bíblica Iberoamericana | © Copyright — requiere licencia |
| Reina-Valera 1960 | RV60 / RVR60 | Sociedades Bíblicas Unidas | © Copyright — requiere licencia |
| Reina-Valera 1995 | RV95 | Sociedades Bíblicas Unidas | © Copyright — requiere licencia |
| Reina-Valera 2020 | RV2020 | Editorial Vida | © Copyright — requiere licencia |
| Nueva Traducción Viviente | NTV | Tyndale House | © Copyright — requiere licencia |
| Nueva Versión Internacional | NVI | Biblica | © Copyright — requiere licencia |
| La Biblia de las Américas | LBLA | Lockman Foundation | © Copyright — requiere licencia |
| Dios Habla Hoy | DHH | Sociedades Bíblicas Unidas | © Copyright — requiere licencia |
| Palabra de Dios para Todos | PDT | Centro Mundial de Traducción | © Copyright — requiere licencia |

- [ ] Documentar el proceso de solicitud de licencia para cada editorial
- [ ] Crear plantilla de carta/correo para contactar editoriales
- [ ] Añadir soporte en el pipeline para importar traducciones con licencia (formato USFM/OSIS)

## 7.5 Auditoría de Datos Reales vs. Placeholders

Revisar **TODOS** los módulos. Si un módulo usa datos hardcodeados que no
provienen de la seed DB SQL, **borrarlos y dejar el módulo vacío de datos**
(estructura y UI intactas, pero sin datos fake).

### Regla general
> Si los datos no vienen de `biblestudio-seed.db` vía SQLDelight queries,
> se eliminan. El módulo mostrará su estado vacío ("No data available").

### Auditoría por módulo
- [ ] **Bible Reader** — Verificar: datos de `bibles`/`verses` tables. Sin hardcodeo.
- [ ] **Cross-References** — Verificar: `crossRefsFromVerse()` query. Sin stubs.
- [ ] **Word Study** — Verificar: `lexicon_entries` + `word_occurrences`. Sin datos fake.
- [ ] **Morphology / Interlinear** — Verificar: tabla `morphology`. Sin datos de ejemplo.
- [ ] **Passage Guide** — Verificar: agrega queries reales. Sin texto de ejemplo.
- [ ] **Knowledge Graph** — Verificar: `entities` + `relationships`. Sin nodos hardcodeados.
- [ ] **Theological Atlas** — Verificar: `geographic_locations`. Sin coordenadas fake.
- [ ] **Timeline** — Verificar: `timeline_events`. Sin eventos hardcodeados.
- [ ] **Reading Plans** — Verificar: `BuiltInPlans.kt` UUIDs = seed DB UUIDs. Sin planes fake.
- [ ] **Note Editor** — Verificar: CRUD vía `notes` table. Sin notas de ejemplo.
- [ ] **Sermon Editor** — Verificar: CRUD vía `sermons` table. Sin sermones de ejemplo.
- [ ] **Search** — Verificar: FTS5 queries. Sin resultados fake.
- [ ] **Highlights** — Verificar: CRUD vía `highlights` table. Sin highlights de ejemplo.
- [ ] **Bookmarks** — Verificar: CRUD vía `bookmarks` table. Sin bookmarks de ejemplo.
- [ ] **Dashboard** — Verificar: estadísticas reales de la DB. Sin números fake.
- [ ] **Resource Library** — Verificar: lista módulos de la DB. Sin módulos fake.
- [ ] **Module System** — Verificar: gestión real de módulos. Sin datos stub.
- [ ] **Import/Export** — Verificar: importa/exporta datos reales. Sin datos de ejemplo.
- [ ] **Settings** — Verificar: lee/escribe `app_settings`. Sin config hardcodeada.
- [ ] **Exegetical Guide** — Verificar: agrega queries reales. Sin texto de ejemplo.
- [ ] **Audio Sync** — Verificar: estado vacío si no hay audio. Sin audio fake.
- [ ] **Text Comparison** — Verificar: compara traducciones reales. Sin texto de ejemplo.
- [ ] **Syntax Search** — Verificar: busca en morfología real. Sin resultados fake.

### Archivos sospechosos de revisar
- [ ] `PaneRegistry.init()` — Los placeholder builders (`Napier.w("Placeholder pane…")`) deben ser reemplazados por builders reales de cada feature module
- [ ] `DefaultSettingsComponent.PLACEHOLDER_TIMESTAMP` — Reemplazar por `Clock.System.now()` o similar
- [ ] `DefaultWorkspaceComponent.autoSaveTimestamp` — Reemplazar por timestamp real

---

## Phase 7 Exit Criteria

- [ ] Los 22+ pane types se abren sin crash y muestran datos reales de la seed DB
- [ ] VerseBus propaga cambios de versículo a todos los módulos suscritos
- [ ] Layout tipo VSCode: splits, tabs, presets, resize, move, persistencia — todo funcional
- [ ] Al menos 1 traducción en español (dominio público) disponible en la seed DB
- [ ] Tabla de traducciones con copyright documentada con plan de licenciamiento
- [ ] Cero módulos con datos hardcodeados/placeholders (todo viene de la seed DB o está vacío)
- [ ] `PaneRegistry` placeholder builders reemplazados por builders reales
- [ ] Timestamps de placeholder reemplazados por valores reales (`Clock`)
