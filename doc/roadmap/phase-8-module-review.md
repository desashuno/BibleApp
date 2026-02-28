# Phase 8 — Revisión de Módulos y Nuevas Fuentes de Datos

> Auditoría individual de cada módulo, búsqueda e integración de nuevas fuentes
> de datos abiertas, y validación cruzada entre módulos.
> **Prerequisites**: Phase 7 (Verificación Integral) complete; todos los módulos libres de placeholders.

---

## 8.1 Revisión Individual de Módulos

Auditoría profunda de cada módulo: calidad de datos, completitud, UX, rendimiento
y cobertura de tests.

### Bible Reader
- [ ] Revisar cobertura de libros: 66 libros canónicos × N traducciones
- [ ] Verificar conteo de versículos por libro coincide con referencia estándar
- [ ] Revisar rendimiento de carga de capítulo (target < 100ms)
- [ ] Verificar que caracteres especiales (hebreo, griego, acentos) se muestran correctamente
- [ ] Revisar UX: navegación libro → capítulo → versículo fluida

### Cross-References
- [ ] Verificar cobertura: ≥ 300K referencias cruzadas en la seed DB
- [ ] Revisar distribución: todos los libros tienen al menos algunas referencias
- [ ] Verificar que el score de confianza (`confidence`) tiene valores útiles para ordenar
- [ ] Revisar UX: heatmap de colores por confianza legible y consistente

### Word Study & Lexicon
- [ ] Verificar cobertura: ≥ 19K entradas de lexicón (hebreo + griego)
- [ ] Revisar que las definiciones tienen contenido útil (no truncadas ni vacías)
- [ ] Verificar `word_occurrences` tiene datos para las palabras más comunes
- [ ] Revisar UX: flujo de clic en palabra → estudio de palabra fluido

### Morphology / Interlinear
- [ ] Verificar cobertura: ≥ 427K palabras morfológicas
- [ ] Revisar que `ParsingDecoder` maneja todos los códigos TAHOT/TAGNT
- [ ] Verificar alineación interlineal: hebreo/griego ↔ español/inglés
- [ ] Revisar UX: grid interlineal legible con anotaciones claras

### Knowledge Graph
- [ ] Verificar cobertura: ≥ 14K entidades con tipos correctos (Person, Place, Event, etc.)
- [ ] Revisar que las relaciones entre entidades son coherentes
- [ ] Verificar rendimiento del canvas con 200+ nodos (target 60 fps)
- [ ] Revisar UX: navegación de grafo intuitiva, zoom, pan

### Theological Atlas
- [ ] Verificar cobertura: ≥ 1,335 ubicaciones geográficas con coordenadas válidas
- [ ] Revisar que las coordenadas corresponden a ubicaciones reales (no genéricas)
- [ ] Verificar que el tile cache funciona offline
- [ ] Revisar UX: zoom, pins clicables, popup informativo

### Timeline
- [ ] Verificar cobertura: ≥ 68 eventos con fechas y eras coherentes
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
- [ ] Verificar que agrega datos de múltiples fuentes correctamente
- [ ] Revisar utilidad del reporte exegético generado

### Audio Sync
- [ ] Verificar estado vacío correcto (sin audio disponible)
- [ ] Revisar preparación para futura integración de audio

### Text Comparison & Syntax Search
- [ ] Verificar comparación lado a lado de traducciones reales
- [ ] Revisar búsqueda sintáctica en datos morfológicos reales

### Resource Library & Module System
- [ ] Verificar lista de módulos instalados
- [ ] Revisar flujo de instalación/desinstalación de módulos

## 8.2 Nuevas Fuentes de Datos

Buscar e investigar fuentes de datos abiertas adicionales para enriquecer
la aplicación.

### Comentarios Bíblicos
- [ ] Investigar Matthew Henry Commentary (dominio público)
- [ ] Investigar John Gill's Exposition (dominio público)
- [ ] Investigar Adam Clarke Commentary (dominio público)
- [ ] Investigar Jamieson-Fausset-Brown Commentary (dominio público)
- [ ] Evaluar formato disponible (XML, texto plano, etc.)
- [ ] Crear normalizer `normalizers/commentaries.py` si se encuentran fuentes viables

### Diccionarios Teológicos
- [ ] Investigar Easton's Bible Dictionary (dominio público)
- [ ] Investigar Smith's Bible Dictionary (dominio público)
- [ ] Investigar International Standard Bible Encyclopedia (ISBE) — edición de dominio público
- [ ] Evaluar integración con el módulo Resource Library

### Concordancias
- [ ] Investigar Strong's Concordance (datos base en dominio público)
- [ ] Verificar cobertura actual de Strong's numbers en el lexicón
- [ ] Evaluar si se necesita un normalizer adicional

### Traducciones Adicionales
- [ ] Investigar traducciones en otros idiomas con licencia libre
- [ ] Buscar repositorios open-source de Biblias (eBible.org, SWORD modules, etc.)
- [ ] Evaluar formato USFM/OSIS y añadir soporte de importación al pipeline
- [ ] Buscar traducciones españolas de dominio público en repositorios digitales

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
- [ ] Actualizar el schema SQL si se necesitan nuevas tablas
- [ ] Actualizar las tablas FTS5 para indexar nuevo contenido
- [ ] Añadir nuevos thresholds al flag `--verify` de `normalize.py`
- [ ] Actualizar `doc/OPEN_DATA_SOURCES.md` con cada nueva fuente, licencia y atribución
- [ ] Actualizar el CI pipeline para incluir las nuevas fuentes

## 8.4 Validación Cruzada entre Módulos

Verificar consistencia de datos entre módulos que comparten información.

- [ ] **Entidades ↔ Atlas**: todas las entidades tipo Place tienen coordenadas en `geographic_locations`
- [ ] **Entidades ↔ Timeline**: entidades tipo Event aparecen en `timeline_events`
- [ ] **Cross-refs ↔ Versículos**: todos los `globalVerseId` en cross-refs existen en `verses`
- [ ] **Morphology ↔ Lexicon**: todos los Strong's numbers en morphology tienen entrada en `lexicon_entries`
- [ ] **Reading Plans ↔ Versículos**: todos los rangos de lectura apuntan a versículos existentes
- [ ] **Entity-Verse Index ↔ Ambos**: cada `entity_id` y `verse_id` en el índice existe
- [ ] **Location-Verse Index ↔ Ambos**: cada `location_id` y `verse_id` en el índice existe
- [ ] Crear script de validación cruzada: `python normalize.py --cross-validate`

---

## Phase 8 Exit Criteria

- [ ] Todos los 22+ módulos auditados individualmente con calidad aceptable
- [ ] Al menos 2 nuevas fuentes de datos integradas (ej: comentario + diccionario)
- [ ] Pipeline ampliado con nuevos normalizers y verificación
- [ ] Validación cruzada pasa sin errores de consistencia
- [ ] `doc/OPEN_DATA_SOURCES.md` actualizado con todas las nuevas fuentes
- [ ] Cobertura de tests ≥ 80% en módulos de datos críticos
