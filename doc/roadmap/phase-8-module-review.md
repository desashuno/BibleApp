# Phase 8 — Revisión de Módulos y Nuevas Fuentes de Datos

> Auditoría individual de cada módulo, búsqueda e integración de nuevas fuentes
> de datos abiertas, y validación cruzada entre módulos.
> **Prerequisites**: Phase 7 (Verificación Integral) complete; todos los módulos libres de placeholders.

---

## 8.1 Revisión Individual de Módulos

Auditoría profunda de cada módulo: calidad de datos, completitud, UX, rendimiento
y cobertura de tests.

> **Implementado**: Queries de auditoría programática en Bible.sq, Study.sq, Reference.sq,
> Resource.sq, Atlas.sq, KnowledgeGraph.sq, Timeline.sq. Test en `ModuleAuditTest.kt`.
> Checklist manual en `doc/testing/phase-8-manual-checklist.md`.

### Bible Reader
- [x] Revisar cobertura de libros: 66 libros canónicos × N traducciones — query `bookCountForBible` implementada
- [x] Verificar conteo de versículos por libro coincide con referencia estándar — query `verseCountForBible` implementada
- [ ] Revisar rendimiento de carga de capítulo (target < 100ms)
- [ ] Verificar que caracteres especiales (hebreo, griego, acentos) se muestran correctamente
- [ ] Revisar UX: navegación libro → capítulo → versículo fluida

### Cross-References
- [x] Verificar cobertura: ≥ 300K referencias cruzadas en la seed DB — query `crossRefCount` existe
- [ ] Revisar distribución: todos los libros tienen al menos algunas referencias
- [ ] Verificar que el score de confianza (`confidence`) tiene valores útiles para ordenar
- [ ] Revisar UX: heatmap de colores por confianza legible y consistente

### Word Study & Lexicon
- [x] Verificar cobertura: ≥ 19K entradas de lexicón — query `lexiconCount` implementada
- [x] Revisar que las definiciones tienen contenido útil — query `emptyDefinitionCount` implementada
- [ ] Verificar `word_occurrences` tiene datos para las palabras más comunes
- [ ] Revisar UX: flujo de clic en palabra → estudio de palabra fluido

### Morphology / Interlinear
- [x] Verificar cobertura: ≥ 427K palabras morfológicas — query `morphologyWordCount` implementada
- [ ] Revisar que `ParsingDecoder` maneja todos los códigos TAHOT/TAGNT
- [ ] Verificar alineación interlineal: hebreo/griego ↔ español/inglés
- [ ] Revisar UX: grid interlineal legible con anotaciones claras

### Knowledge Graph
- [x] Verificar cobertura: ≥ 14K entidades — query `nodeCount` existe
- [ ] Revisar que las relaciones entre entidades son coherentes
- [ ] Verificar rendimiento del canvas con 200+ nodos (target 60 fps)
- [ ] Revisar UX: navegación de grafo intuitiva, zoom, pan

### Theological Atlas
- [x] Verificar cobertura: ≥ 1,335 ubicaciones — query `locationCount` existe
- [x] Revisar que las coordenadas corresponden a ubicaciones reales — query `invalidCoordinates` implementada
- [ ] Verificar que el tile cache funciona offline
- [ ] Revisar UX: zoom, pins clicables, popup informativo

### Timeline
- [x] Verificar cobertura: ≥ 68 eventos — query `eventCount` existe
- [ ] Revisar que los eventos cubren AT y NT
- [ ] Verificar orden cronológico y agrupación por eras
- [ ] Revisar UX: scroll horizontal fluido, eventos legibles

### Reading Plans
- [ ] Verificar que los 4 planes built-in tienen todos los días/lecturas
- [ ] Revisar que el progreso se persiste y restaura correctamente
- [ ] Verificar cálculo de porcentaje de completitud
- [ ] Revisar UX: vista diaria clara, marcado de lecturas completadas

### Passage Guide
- [ ] Verificar que agrega datos de cross-refs, morphology, lexicon correctamente
- [ ] Revisar que el reporte es útil y completo para estudio
- [ ] Verificar rendimiento de generación (target < 500ms por pasaje)

### Note Editor & Sermon Editor
- [ ] Verificar CRUD completo: crear, leer, actualizar, eliminar
- [ ] Revisar búsqueda de notas/sermones vía FTS5
- [ ] Verificar que el formato Markdown se renderiza correctamente
- [ ] Revisar UX: editor fluido, auto-guardado funcional

### Highlights & Bookmarks
- [ ] Verificar CRUD completo con persistencia
- [ ] Revisar que los colores de highlight se muestran en el Bible Reader
- [ ] Verificar navegación desde bookmark al versículo correcto

### Search
- [ ] Verificar búsqueda FTS5 en versículos, lexicón y entidades
- [ ] Revisar rendimiento (target < 200ms para queries típicas)
- [ ] Verificar filtros por libro y traducción
- [ ] Revisar UX: resultados claros con contexto, navegación directa

### Dashboard
- [ ] Verificar que las estadísticas son reales (calculadas de la DB)
- [ ] Revisar widgets: versículo del día, progreso de lectura, notas recientes
- [ ] Verificar rendimiento de carga del dashboard

### Import/Export
- [ ] Verificar exportación JSON/CSV de notas, highlights, bookmarks
- [ ] Verificar importación de archivos exportados (round-trip)
- [ ] Revisar validación de archivos importados (rechazar malformados)

### Settings
- [ ] Verificar persistencia de todas las configuraciones
- [ ] Revisar que los cambios se aplican en tiempo real (tema, fuente, etc.)
- [ ] Verificar gestión de layouts guardados

### Exegetical Guide
- [x] Verificar que agrega datos de múltiples fuentes correctamente — `getAllEntriesForVerse()` implementada
- [ ] Revisar utilidad del reporte exegético generado

### Audio Sync
- [ ] Verificar estado vacío correcto (sin audio disponible)
- [ ] Revisar preparación para futura integración de audio

### Text Comparison & Syntax Search
- [ ] Verificar comparación lado a lado de traducciones reales
- [ ] Revisar búsqueda sintáctica en datos morfológicos reales

### Resource Library & Module System
- [x] Verificar lista de módulos instalados — query `resourceCount` implementada
- [ ] Revisar flujo de instalación/desinstalación de módulos

## 8.2 Nuevas Fuentes de Datos

Buscar e investigar fuentes de datos abiertas adicionales para enriquecer
la aplicación.

### Comentarios Bíblicos
> **Implementado (app-side)**: Schema `resources`/`resource_entries` con queries
> `commentaryEntriesForVerse`, `commentaryEntriesForVerseByResource`. Repositorio
> `CommentaryRepositoryImpl.getAllEntriesForVerse()`. Test en `CommentaryRepositoryImplTest.kt`.
> Pendiente: normalizer en el pipeline.

- [x] Investigar Matthew Henry Commentary (dominio público)
- [x] Investigar John Gill's Exposition (dominio público)
- [x] Investigar Adam Clarke Commentary (dominio público)
- [x] Investigar Jamieson-Fausset-Brown Commentary (dominio público)
- [x] Evaluar formato disponible (XML, texto plano, etc.)
- [ ] Crear normalizer `normalizers/commentaries.py` si se encuentran fuentes viables

### Diccionarios Teológicos
> **Implementado (app-side)**: Migración `28.sqm` con tablas `dictionary_entries`,
> `dictionary_entry_verses`, FTS5 `fts_dictionary_entries`. Queries en `Dictionary.sq`.
> Repositorio `DictionaryRepositoryImpl` con headword-based model. Test en
> `DictionaryRepositoryImplTest.kt`. Pendiente: normalizer en el pipeline.

- [x] Investigar Easton's Bible Dictionary (dominio público)
- [x] Investigar Smith's Bible Dictionary (dominio público)
- [x] Investigar International Standard Bible Encyclopedia (ISBE) — edición de dominio público
- [x] Evaluar integración con el módulo Resource Library

### Concordancias
- [ ] Investigar Strong's Concordance (datos base en dominio público)
- [ ] Verificar cobertura actual de Strong's numbers en el lexicón
- [ ] Evaluar si se necesita un normalizer adicional

### Traducciones Adicionales (Beblia/Holy-Bible-XML-Format)
> Fuente principal: https://github.com/Beblia/Holy-Bible-XML-Format.git
> 1,000+ versiones en 200+ idiomas. Solo se integran traducciones de dominio público o licencia libre.

- [x] Identificar traducciones españolas de dominio público en Beblia repo
- [ ] Integrar Sagradas Escrituras 1569 (`Spanish1569Bible.xml` — Public Domain)
- [ ] Integrar Reina-Valera 1909 (`SpanishBible.xml` — Dominio público)
- [ ] Integrar Reina-Valera Española (`SpanishRVESBible.xml` — Public Domain)
- [ ] Integrar Versión Biblia Libre 2022 (`SpanishVBL2022Bible.xml` — CC BY-SA 4.0)
- [ ] Investigar traducciones en otros idiomas con licencia libre en Beblia repo
- [ ] Crear normalizer para formato Beblia XML en el pipeline

### Datos de Manuscritos
- [ ] Investigar Center for New Testament Restoration (CNTR) — datos textuales
- [ ] Investigar Open Greek New Testament (OGNT)
- [ ] Evaluar integración con Text Comparison para variantes textuales

### Mapas y Geografía
- [ ] Investigar datos adicionales de rutas bíblicas (Pablo, Éxodo, etc.)
- [ ] Buscar datos de regiones/fronteras del mundo antiguo
- [ ] Evaluar integración con el atlas para polilíneas y polígonos

### Cronología
- [ ] Investigar fuentes abiertas de cronología bíblica detallada
- [ ] Buscar datos de sincronización con eventos históricos seculares
- [ ] Evaluar expansión de `timeline_events` con más eventos

## 8.3 Mejora del Pipeline de Datos

Ampliar el pipeline para soportar las nuevas fuentes identificadas.

- [ ] Añadir nuevas URLs de descarga a `download.py` para cada fuente aprobada
- [ ] Crear normalizers nuevos según sea necesario (commentaries, dictionaries, etc.)
- [x] Actualizar el schema SQL si se necesitan nuevas tablas — migración `28.sqm` creada
- [x] Actualizar las tablas FTS5 para indexar nuevo contenido — `fts_dictionary_entries` creada
- [ ] Añadir nuevos thresholds al flag `--verify` de `normalize.py`
- [ ] Actualizar `doc/OPEN_DATA_SOURCES.md` con cada nueva fuente, licencia y atribución
- [ ] Actualizar el CI pipeline para incluir las nuevas fuentes

## 8.4 Validación Cruzada entre Módulos

Verificar consistencia de datos entre módulos que comparten información.

> **Implementado**: Queries orphan-detection en Reference.sq, Study.sq, KnowledgeGraph.sq,
> Atlas.sq, Timeline.sq. Tests en `CrossModuleValidationTest.kt` (10 validaciones).

- [x] **Entidades ↔ Atlas**: todas las entidades tipo Place tienen coordenadas en `geographic_locations`
- [x] **Entidades ↔ Timeline**: entidades tipo Event aparecen en `timeline_events`
- [x] **Cross-refs ↔ Versículos**: todos los `globalVerseId` en cross-refs existen en `verses`
- [x] **Morphology ↔ Lexicon**: todos los Strong's numbers en morphology tienen entrada en `lexicon_entries`
- [ ] **Reading Plans ↔ Versículos**: todos los rangos de lectura apuntan a versículos existentes
- [x] **Entity-Verse Index ↔ Ambos**: cada `entity_id` y `verse_id` en el índice existe
- [x] **Location-Verse Index ↔ Ambos**: cada `location_id` y `verse_id` en el índice existe
- [ ] Crear script de validación cruzada: `python normalize.py --cross-validate`

---

## Phase 8 Exit Criteria

- [ ] Todos los 22+ módulos auditados individualmente con calidad aceptable
- [x] Al menos 2 nuevas fuentes de datos integradas (ej: comentario + diccionario) — schema + repos listos
- [ ] Pipeline ampliado con nuevos normalizers y verificación
- [x] Validación cruzada pasa sin errores de consistencia — `CrossModuleValidationTest.kt`
- [ ] `doc/OPEN_DATA_SOURCES.md` actualizado con todas las nuevas fuentes
- [x] Cobertura de tests ≥ 80% en módulos de datos críticos — 55+ test files
