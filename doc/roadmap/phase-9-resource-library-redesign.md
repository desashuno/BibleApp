# Phase 9 — Rediseño del Resource Library y Gestión Centralizada de Datos

> Unificación de `resource_library`, `module_system` y `resource_downloader` en una
> arquitectura centralizada de gestión de datos. Nuevo `DataManager` como punto único
> de descarga, instalación, habilitación y eliminación de módulos de datos.
> **Prerequisites**: Phase 8 (Revisión de Módulos) complete; pipeline de datos funcional.

---

## 9.1 DataManager: Clase Centralizada de Gestión de Datos

Nueva clase singleton en `shared/.../core/data_manager/` que posee TODAS las operaciones
de datos (descarga, instalación, habilitación/deshabilitación, eliminación). Registrada
como singleton Koin. Expone `StateFlow<DataManagerState>` con el estado de todos los
módulos de datos.

### Modelo de datos

- [ ] Crear `DataModuleDescriptor` — metadatos de un módulo descargable (nombre, tipo, tamaño, URL fuente, licencia, versión)
- [ ] Crear `DataModuleStatus` — sealed class: `NotInstalled`, `Downloading(progress: Float)`, `Installed`, `Enabled`, `Disabled`, `Error(message: String)`
- [ ] Crear `DataModuleType` — enum: `Bible`, `Commentary`, `Dictionary`, `Morphology`, `CrossReferences`, `Geography`, `Entities`, `Timeline`
- [ ] Crear `DataManagerState` — data class con `Map<String, DataModuleStatus>` y estado de cola de descargas

### Interfaz DataModuleHandler

- [ ] Crear interfaz `DataModuleHandler` con contrato para cada tipo de módulo:
  - `suspend fun install(descriptor: DataModuleDescriptor, data: ByteArray): Result<Unit>`
  - `suspend fun uninstall(moduleId: String): Result<Unit>`
  - `suspend fun validate(moduleId: String): Result<Boolean>`
  - `fun supportedType(): DataModuleType`
- [ ] Cada handler encapsula el parseo y almacenamiento específico de su tipo de módulo

### DataManager

- [ ] Crear `DataManager` en `core/data_manager/DataManager.kt`
- [ ] Implementar cola de descargas con `Channel` / `Mutex` para descargas secuenciales
- [ ] Exponer `StateFlow<DataManagerState>` con estado agregado de todos los módulos
- [ ] Métodos públicos: `download(descriptor)`, `install(descriptor)`, `enable(moduleId)`, `disable(moduleId)`, `remove(moduleId)`, `downloadAll()`
- [ ] Persistir estado de módulos en tabla `data_modules` (ver 9.5)
- [ ] Registrar como singleton Koin en `CoreModule`
- [ ] Inyectar todos los `DataModuleHandler` como `List<DataModuleHandler>` vía Koin

---

## 9.2 Estándar de Acceso a Datos para Módulos

Documentar y aplicar el patrón estándar que todos los features deben seguir.

### Principios

- [ ] **Lectura**: los repositorios siguen leyendo de tablas SQLDelight (sin cambios en el path de lectura)
- [ ] **Escritura de ciclo de vida**: DataManager es el único escritor para instalación, habilitación, deshabilitación y eliminación de módulos
- [ ] **Handlers**: las implementaciones de `DataModuleHandler` encapsulan el parseo y almacenamiento específico de cada tipo
- [ ] **Dependencias**: los features declaran dependencias de datos vía Koin — reciben interfaces de repositorio, nunca interactúan con DataManager directamente (excepto el UI del Resource Library)

### Documentación

- [ ] Actualizar `doc/DATA_LAYER.md` con la nueva arquitectura de DataManager
- [ ] Actualizar `doc/MODULE_SYSTEM.md` describiendo el nuevo flujo de módulos
- [ ] Agregar diagramas de flujo: descarga → parseo → almacenamiento → habilitación

---

## 9.3 Nueva Estructura `features/modules/`

Nuevo directorio bajo `features/` para los handlers de módulos de datos. Cada handler
implementa `DataModuleHandler` y encapsula el parseo y almacenamiento de su tipo.

```
features/modules/
├── bible/          — BibleModuleHandler (OSIS/USFM/Sword → bibles/verses)
├── commentary/     — CommentaryModuleHandler (→ resource_entries type=commentary)
├── dictionary/     — DictionaryModuleHandler (→ dictionary_entries)
├── morphology/     — MorphologyModuleHandler (→ morphology/word_occurrences)
├── cross_refs/     — CrossRefsModuleHandler (→ cross_references)
├── geography/      — GeographyModuleHandler (→ atlas_locations)
├── entities/       — EntitiesModuleHandler (→ graph_nodes/graph_edges)
└── timeline/       — TimelineModuleHandler (→ timeline_events)
```

### Handlers individuales

- [ ] Crear `BibleModuleHandler` — migrar parsers de `module_system/data/parsers/` (`OsisParser`, `UsfmParser`, `SwordParser`)
- [ ] Crear `CommentaryModuleHandler` — parseo de comentarios → `resource_entries`
- [ ] Crear `DictionaryModuleHandler` — parseo de diccionarios → `dictionary_entries`
- [ ] Crear `MorphologyModuleHandler` — parseo de datos morfológicos → `morphology`, `word_occurrences`
- [ ] Crear `CrossRefsModuleHandler` — parseo de referencias cruzadas → `cross_references`
- [ ] Crear `GeographyModuleHandler` — parseo de datos geográficos → `geographic_locations`
- [ ] Crear `EntitiesModuleHandler` — parseo de entidades → `graph_nodes`, `graph_edges`
- [ ] Crear `TimelineModuleHandler` — parseo de eventos → `timeline_events`
- [ ] Registrar todos los handlers como `List<DataModuleHandler>` en Koin (`ComponentModule` o nuevo `ModuleHandlerModule`)

---

## 9.4 Rediseño del Resource Library (UI)

Eliminar los features actuales `resource_library` y `module_system`. Nuevo Resource Library
unificado como pane único que delega TODA la lógica al DataManager.

### Nuevo ResourceLibraryComponent

- [ ] Crear `ResourceLibraryComponent` (interfaz) y `DefaultResourceLibraryComponent` (impl)
- [ ] Inyectar `DataManager` como dependencia principal
- [ ] Exponer `StateFlow<ResourceLibraryState>` con:
  - Lista de módulos con su estado actual (`DataModuleStatus`)
  - Filtro activo por categoría (`DataModuleType?`)
  - Término de búsqueda
  - Modo de vista (lista / grid)
- [ ] Métodos: `downloadModule(id)`, `removeModule(id)`, `toggleEnabled(id)`, `downloadAll()`, `setFilter(type)`, `setSearchQuery(query)`

### UI del Resource Library Pane

- [ ] Mostrar TODOS los módulos de datos en lista categorizada (Biblias, Comentarios, Diccionarios, Herramientas de Estudio, etc.)
- [ ] Cada módulo muestra estado: no instalado / descargando / instalado / habilitado / deshabilitado
- [ ] Toggle de habilitación/deshabilitación por módulo
- [ ] Botón de descarga individual y botón "Descargar Todo"
- [ ] Botón de eliminación para módulos instalados (con confirmación)
- [ ] Barra de progreso de descarga (por módulo y agregada)
- [ ] Búsqueda/filtro por nombre y categoría
- [ ] Información de cada módulo: nombre, descripción, tamaño, licencia, versión

### Pane Registration

- [ ] Registrar nuevo `resource-library` pane type en `PaneRegistry`
- [ ] Actualizar `PaneContent.kt` para instanciar `DefaultResourceLibraryComponent` con Koin
- [ ] Crear `ResourceLibraryPane.kt` en `composeApp/.../ui/panes/`

---

## 9.5 Migración de Base de Datos

Nueva tabla `data_modules` unificada que reemplaza `installed_modules` y `resources`
para metadatos de módulos. Las tablas de contenido permanecen sin cambios.

- [ ] Crear nueva migración `.sqm` (siguiente número después de la última existente)
- [ ] Tabla `data_modules`:
  - `id TEXT PRIMARY KEY` — identificador único del módulo
  - `name TEXT NOT NULL` — nombre para mostrar
  - `type TEXT NOT NULL` — tipo de módulo (`bible`, `commentary`, `dictionary`, etc.)
  - `version TEXT NOT NULL` — versión del módulo
  - `description TEXT` — descripción del módulo
  - `license TEXT` — licencia (Public Domain, CC BY-SA 4.0, etc.)
  - `sourceUrl TEXT` — URL de descarga
  - `sizeBytes INTEGER` — tamaño en bytes
  - `status TEXT NOT NULL DEFAULT 'installed'` — `installed`, `enabled`, `disabled`
  - `installedAt TEXT NOT NULL` — timestamp ISO 8601
  - `updatedAt TEXT` — timestamp de última actualización
- [ ] Migrar datos existentes de `installed_modules` → `data_modules`
- [ ] Migrar metadatos de `resources` → `data_modules`
- [ ] Crear queries SQLDelight en nuevo archivo `DataModule.sq`:
  - `allModules`, `moduleById`, `modulesByType`, `modulesByStatus`
  - `insertModule`, `updateStatus`, `deleteModule`
  - `enabledModulesByType`, `downloadProgress`
- [ ] Actualizar `DATABASE_VERSION` en `AppInfo.kt`
- [ ] Mantener tablas `installed_modules` y `resources` hasta que la migración esté verificada

> **Nota**: Las tablas de contenido (`verses`, `resource_entries`, `dictionary_entries`,
> `morphology`, `cross_references`, `geographic_locations`, `graph_nodes`, `timeline_events`)
> NO se modifican. Solo se unifica la tabla de metadatos de módulos.

---

## 9.6 Limpieza y Eliminación de Código Obsoleto

Eliminar código antiguo que queda redundante tras la unificación.

### Features a eliminar

- [ ] Eliminar `features/resource_library/` — reemplazado por nuevo Resource Library en 9.4
- [ ] Eliminar `features/module_system/` — reemplazado por `features/modules/` (handlers) y `core/data_manager/`
- [ ] Eliminar `features/resource_downloader/` — scaffold vacío, funcionalidad absorbida por DataManager

### DI cleanup

- [ ] Eliminar registros de `ModuleRepository`, `ModuleRepositoryImpl` de `RepositoryModule`
- [ ] Eliminar registros de `ResourceRepository`, `ResourceRepositoryImpl` de `RepositoryModule`
- [ ] Eliminar registros de `ModuleManagerComponent`, `DefaultModuleManagerComponent` de `ComponentModule`
- [ ] Eliminar registros de `ResourceLibraryComponent` (viejo), `DefaultResourceLibraryComponent` (viejo) de `ComponentModule`
- [ ] Agregar nuevos registros: `DataManager`, handlers, nuevo `ResourceLibraryComponent`

### PaneRegistry & PaneContent

- [ ] Eliminar pane type `module-manager` de `PaneRegistry`
- [ ] Actualizar `PaneContent.kt`: reemplazar dispatch viejo de `resource-library` y `module-manager` con nuevo `ResourceLibraryComponent`
- [ ] Eliminar `ModuleManagerPane.kt` del composeApp si existe

### Limpieza general

- [ ] Buscar y eliminar imports huérfanos que referencien clases eliminadas
- [ ] Verificar que no quedan referencias a `InstalledModule`, `ModuleSource`, `ModuleMappers`
- [ ] Ejecutar `./gradlew compileKotlinDesktop` para verificar compilación limpia

---

## 9.7 Tests

Cobertura completa de la nueva arquitectura de gestión de datos.

### Unit tests para DataManager

- [ ] Test: estado inicial con todos los módulos como `NotInstalled`
- [ ] Test: `download()` cambia estado a `Downloading` → `Installed`
- [ ] Test: `enable()` / `disable()` alterna estado correctamente
- [ ] Test: `remove()` elimina datos y cambia estado a `NotInstalled`
- [ ] Test: `downloadAll()` procesa todos los módulos secuencialmente
- [ ] Test: manejo de errores de red durante descarga

### Unit tests para DataModuleHandlers

- [ ] Test `BibleModuleHandler`: install parsea OSIS/USFM correctamente → verifica datos en `verses`
- [ ] Test `CommentaryModuleHandler`: install → verifica datos en `resource_entries`
- [ ] Test `DictionaryModuleHandler`: install → verifica datos en `dictionary_entries`
- [ ] Test `MorphologyModuleHandler`: install → verifica datos en `morphology`
- [ ] Test `CrossRefsModuleHandler`: install → verifica datos en `cross_references`
- [ ] Test `GeographyModuleHandler`: install → verifica datos en `geographic_locations`
- [ ] Test `EntitiesModuleHandler`: install → verifica datos en `graph_nodes`, `graph_edges`
- [ ] Test `TimelineModuleHandler`: install → verifica datos en `timeline_events`

### Integration tests

- [ ] Test ciclo completo: install → enable → query data → disable → remove → verify data cleaned
- [ ] Test migración: datos en `installed_modules`/`resources` migran correctamente a `data_modules`
- [ ] Test concurrencia: múltiples descargas en cola se procesan sin conflictos

### UI tests

- [ ] Test Resource Library Pane: renderiza lista de módulos con estados correctos
- [ ] Test filtro por categoría: solo muestra módulos del tipo seleccionado
- [ ] Test toggle enable/disable: botón actualiza estado del módulo
- [ ] Test barra de progreso: muestra progreso durante descarga simulada

---

## Phase 9 Exit Criteria

- [ ] `DataManager` implementado como singleton Koin con `StateFlow<DataManagerState>`
- [ ] 8 `DataModuleHandler` implementados (Bible, Commentary, Dictionary, Morphology, CrossRefs, Geography, Entities, Timeline)
- [ ] Tabla `data_modules` creada con migración que preserva datos existentes
- [ ] Resource Library pane unificado muestra todos los módulos con descarga, instalación, habilitación y eliminación
- [ ] Features obsoletos eliminados: `resource_library/`, `module_system/`, `resource_downloader/`
- [ ] DI, PaneRegistry y PaneContent actualizados sin referencias a código eliminado
- [ ] Compilación limpia: `./gradlew compileKotlinDesktop` sin errores
- [ ] Tests pasan: ≥ 80% cobertura en `core/data_manager/` y `features/modules/`
- [ ] Documentación actualizada: `DATA_LAYER.md`, `MODULE_SYSTEM.md`
