# BibleStudio — Documentación Técnica

> Centro de documentación del proyecto BibleStudio.
> **Stack**: Kotlin Multiplatform · Compose Multiplatform · SQLDelight · Decompose · Koin

---

## Documentos Fundamentales

| Documento | Contenido | Audiencia |
|-----------|-----------|-----------|
| [**ARCHITECTURE.md**](ARCHITECTURE.md) | Arquitectura del sistema, capas KMP, Koin DI, flujo de datos, manejo de errores con sealed classes, logging con Napier, enrutamiento Decompose, secuencia de arranque | Todos los desarrolladores |
| [**DATA_LAYER.md**](DATA_LAYER.md) | Archivos SQLDelight `.sq`, 27 tablas, 5 tablas FTS5, 8 grupos de consultas, 17 repositorios, IDs globales de versículos, consultas reactivas, migraciones, sincronización | Desarrolladores backend |
| [**DESIGN_SYSTEM.md**](DESIGN_SYSTEM.md) | Identidad Digital Scriptorium, paleta de colores, temas Compose Multiplatform, tipografía, animaciones, componentes, breakpoints responsivos, i18n (EN+ES), accesibilidad | Desarrolladores UI/UX, diseñadores |
| [**PLATFORM_STRATEGY.md**](PLATFORM_STRATEGY.md) | Matriz de 5 plataformas, abstracciones expect/actual, rutas de almacenamiento, selectores de archivos, comandos Gradle, empaquetado jpackage, checklists de release | Todos los desarrolladores, DevOps |
| [**MODULE_SYSTEM.md**](MODULE_SYSTEM.md) | PaneRegistry, catálogo de 21 módulos, Verse Bus (SharedFlow + sealed LinkEvent), LayoutNode, presets de workspace, guía de creación de módulos | Desarrolladores de features |
| [**CODE_CONVENTIONS.md**](CODE_CONVENTIONS.md) | Estructura feature-first, convenciones de nombres Kotlin, patrón de componentes Decompose, patrón de entidades, reglas de imports, convenciones SQLDelight y Composable | Todos los desarrolladores |
| [**GETTING_STARTED.md**](GETTING_STARTED.md) | Prerrequisitos (JDK 17+, Gradle), pasos de configuración, referencia de comandos Gradle, depuración, guía de primera contribución, estrategia de ramas | Nuevos desarrolladores |
| [**TESTING.md**](TESTING.md) | Pirámide de tests, herramientas (Turbine, MockK, driver SQLite en memoria, Compose UI Test), patrones para tests de Component/Query/Repository/Composable/Migration, objetivos de cobertura (80%+) | Todos los desarrolladores |
| [**CI_CD.md**](CI_CD.md) | Workflow de GitHub Actions (basado en Gradle), quality gates (detekt, ktlint, Kover), protección de ramas, proceso de release, versionado | DevOps, mantenedores |
| [**SECURITY.md**](SECURITY.md) | Modelo de amenazas, protección de datos, inventario de datos de usuario, validación de imports, seguridad SQL (parametrización SQLDelight), sanitización de entrada, permisos de plataforma, seguridad de dependencias | Todos los desarrolladores, seguridad |
| [**OPEN_DATA_SOURCES.md**](OPEN_DATA_SOURCES.md) | Fuentes de datos open-source por módulo, licencias, atribución, descripción del pipeline de datos | Todos los desarrolladores, datos |

---

## Documentación de Módulos

Cada módulo está documentado como una carpeta independiente con 7 archivos estandarizados.
Consulta la **plantilla base** en [`modules/_template/`](modules/_template/README.md).

### Estructura de un Módulo

```
modules/{nombre}/
  ├── README.md          → Descripción, categoría, estado, dependencias
  ├── ARCHITECTURE.md    → Capas internas, flujo de datos, DI
  ├── ROUTES.md          → Rutas expuestas/consumidas, Verse Bus, deep links
  ├── DATA_MODEL.md      → Entidades, tablas SQLite, repositorios, consultas
  ├── UI_COMPONENTS.md   → Composables, PaneRegistry, wireframes, responsivo
  ├── COMPONENT_STATE.md → Componentes Decompose, StateFlow, efectos secundarios
  └── ROADMAP.md         → Mejoras P0/P1/P2, features absorbidas
```

### Lectura

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`bible-reader`](modules/bible-reader/README.md) | Lector principal de Escrituras + comparación de texto (sub-feature) | Funcional |
| [`reading-plans`](modules/reading-plans/README.md) | Planes de lectura diaria con seguimiento de progreso | Nuevo |

### Estudio

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`cross-references`](modules/cross-references/README.md) | Referencias cruzadas con renderizado en línea | Funcional |
| [`word-study`](modules/word-study/README.md) | Estudio de palabras, gráficos de uso, dominios semánticos | Funcional |
| [`morphology-interlinear`](modules/morphology-interlinear/README.md) | Morfología e interlineal + interlineal inverso (sub-feature) | Funcional |
| [`passage-guide`](modules/passage-guide/README.md) | Guía de pasaje con bosquejos y pasajes paralelos | Funcional |
| [`exegetical-guide`](modules/exegetical-guide/README.md) | Guía exegética: análisis gramatical + léxico + comentarios | Nuevo |
| [`knowledge-graph`](modules/knowledge-graph/README.md) | Grafo de conocimiento bíblico (personas, lugares, eventos) | Esquema listo |
| [`timeline`](modules/timeline/README.md) | Línea de tiempo interactiva | Esquema listo |
| [`theological-atlas`](modules/theological-atlas/README.md) | Atlas con mapas interactivos y superposiciones | Paquete listo |

### Escritura

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`note-editor`](modules/note-editor/README.md) | Editor de notas WYSIWYG con anclaje a versículos | Funcional |
| [`sermon-editor`](modules/sermon-editor/README.md) | Editor de sermones con modo bosquejo | Paquete listo |

### Herramientas

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`search`](modules/search/README.md) | Búsqueda de texto completo con filtros + búsqueda sintáctica (sub-feature) | Funcional |
| [`highlights`](modules/highlights/README.md) | Resaltados en línea con selección a nivel de carácter | Esquema listo |
| [`workspace`](modules/workspace/README.md) | Espacio de trabajo multi-panel + layouts de inicio rápido (sub-feature) | Funcional |
| [`module-system`](modules/module-system/README.md) | Gestión de módulos de datos bíblicos | Funcional |
| [`import-export`](modules/import-export/README.md) | Importar/Exportar OSIS, USFM, Sword | Funcional |
| [`settings`](modules/settings/README.md) | Configuración, preferencias, atajos de teclado | Funcional |
| [`bookmarks-history`](modules/bookmarks-history/README.md) | Marcadores e historial de navegación | Esquema listo |
| [`dashboard`](modules/dashboard/README.md) | Pantalla de inicio personalizable | Nuevo |

### Recursos

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`resource-library`](modules/resource-library/README.md) | Navegador de comentarios, diccionarios y multimedia | Funcional |

### Multimedia

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| [`audio-sync`](modules/audio-sync/README.md) | Reproducción sincronizada de Biblia en audio | Nuevo |

### Servicios Compartidos

| Servicio | Descripción | Ubicación |
|----------|-------------|-----------|
| [`share-verse`](modules/_shared/share-verse/README.md) | Compartir versículos como texto o imagen estilizada | `modules/_shared/share-verse/` |

---

## Navegación Rápida

### ¿Quieres entender la arquitectura general?
Comienza con [ARCHITECTURE.md](ARCHITECTURE.md)

### ¿Compilando para una plataforma específica?
Ve a [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) §7 — Guías de Compilación

### ¿Necesitas modificar la base de datos?
Consulta [DATA_LAYER.md](DATA_LAYER.md) §3 — Esquema, §8 — Migraciones

### ¿Creando o modificando un componente visual?
Revisa [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) §2–6 — Colores, Tipografía, Componentes

### ¿Primer día en el proyecto?
Sigue [GETTING_STARTED.md](GETTING_STARTED.md) §2 — Configuración

### ¿Agregando un nuevo módulo de panel?
Sigue [MODULE_SYSTEM.md](MODULE_SYSTEM.md) §6 — Guía de Creación de Módulos, luego la [plantilla de módulo](modules/_template/README.md)

### ¿Entendiendo un módulo específico?
Ve a `modules/{nombre}/README.md` — cada uno tiene documentación completa e independiente

---

## Estadísticas del Proyecto

| Métrica | Valor |
|---------|-------|
| Archivos fuente Kotlin | ~400+ |
| Módulos documentados | 22 |
| Servicios compartidos | 1 |
| Entidades de dominio | 33 |
| Interfaces de repositorio | 17 |
| Grupos de consultas SQLDelight | 8 |
| Componentes Decompose | 15+ |
| Pantallas Composable | 25+ |
| Tipos de panel | 21 |
| Composables reutilizables | 37+ |
| Tablas SQLite | 27 |
| Tablas FTS5 | 5 |
| Versión de esquema | 16 |
| Idiomas soportados | 2 (EN, ES) |
| Plataformas objetivo | 5 (Android, iOS, Windows, macOS, Linux) |
| Archivos de test | 25+ |

---

## Otros Archivos de Referencia

| Archivo | Ubicación | Propósito |
|---------|-----------|-----------|
| `CLAUDE.md` | Raíz del proyecto | Orientación rápida para sesiones con IA |
| `README.md` | Raíz del proyecto | Descripción del producto y features |
| `build.gradle.kts` | Raíz del proyecto | Configuración raíz de Gradle |
| `settings.gradle.kts` | Raíz del proyecto | Declaración de módulos |
| `gradle.properties` | Raíz del proyecto | Flags de KMP y Compose |
| `libs.versions.toml` | `gradle/` | Catálogo de versiones de dependencias |
| `detekt.yml` | Raíz del proyecto | Reglas de análisis estático |
| `data-pipeline/` | Raíz del proyecto | Descarga y normalización de datos bíblicos open-source a SQLite |
