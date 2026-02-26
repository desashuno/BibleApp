# Module System

> BibleStudio — Pane Registry, Verse Bus, Workspace Layout & Module Creation Guide

---

## 1. Overview

BibleStudio organizes features into **pane modules** — self-contained units that render inside the workspace layout system. The module system provides:

- **PaneRegistry** — a central catalog that maps type identifiers to `@Composable` builder functions.
- **Verse Bus** — a reactive broadcast channel (`SharedFlow`) for cross-pane navigation.
- **LayoutNode** — a Kotlin `sealed class` tree structure that describes the workspace split layout.
- **WorkspaceComponent** — Decompose component for layout persistence and manipulation.

Each module is independent. Modules do not import each other directly. All inter-module communication flows through the Verse Bus or shared services registered in the Koin container.

---

## 2. PaneRegistry

### 2.1 Registration

The `PaneRegistry` is initialized during bootstrap (see [ARCHITECTURE.md](ARCHITECTURE.md) §8). Each module registers a `@Composable` builder function keyed by a unique `PaneType` string:

```kotlin
class PaneRegistry {
    private val builders = mutableMapOf<String, PaneBuilder>()

    fun init() {
        register("bible_reader") { config -> BibleReaderPane(config) }
        register("search") { config -> SearchPane(config) }
        register("cross_references") { config -> CrossReferencesPane(config) }
        register("word_study") { config -> WordStudyPane(config) }
        register("morphology") { config -> MorphologyPane(config) }
        register("passage_guide") { config -> PassageGuidePane(config) }
        register("resource_library") { config -> ResourceLibraryPane(config) }
        register("note_editor") { config -> NoteEditorPane(config) }
        register("highlights") { config -> HighlightsPane(config) }
        register("knowledge_graph") { config -> KnowledgeGraphPane(config) }
        register("timeline") { config -> TimelinePane(config) }
        register("theological_atlas") { config -> TheologicalAtlasPane(config) }
        register("sermon_editor") { config -> SermonEditorPane(config) }
        register("audio_sync") { config -> AudioSyncPane(config) }
        register("text_comparison") { config -> TextComparisonPane(config) }
        register("reverse_interlinear") { config -> ReverseInterlinearPane(config) }
        register("exegetical_guide") { config -> ExegeticalGuidePane(config) }
        register("reading_plans") { config -> ReadingPlansPane(config) }
        register("syntax_search") { config -> SyntaxSearchPane(config) }
        register("bookmarks") { config -> BookmarksPane(config) }
        register("settings") { config -> SettingsPane(config) }
    }

    fun register(type: String, builder: PaneBuilder) {
        builders[type] = builder
    }

    @Composable
    fun Build(type: String, config: Map<String, String> = emptyMap()) {
        val builder = builders[type]
            ?: throw IllegalArgumentException("Unknown pane type: $type")
        builder(config)
    }

    val availableTypes: List<String> get() = builders.keys.toList()
}

typealias PaneBuilder = @Composable (config: Map<String, String>) -> Unit
```

### 2.2 Pane Metadata

Each pane type has associated metadata for display in the activity bar and module picker:

```kotlin
data class PaneMetadata(
    val type: String,
    val displayName: String,
    val icon: ImageVector,
    val category: PaneCategory,
    val description: String,
)

enum class PaneCategory { Text, Study, Resource, Writing, Tool, Media }
```

---

## 3. Module Catalog

### 3.1 Bible Text

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 1 | Bible Reader | `bible_reader` | Primary Scripture reader with chapter navigation |
| 2 | Text Comparison | `text_comparison` | Side-by-side version comparison |
| 3 | Reverse Interlinear | `reverse_interlinear` | English text with linked original-language words |

### 3.2 Study

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 4 | Cross-References | `cross_references` | Treasury of Scripture Knowledge integration |
| 5 | Word Study | `word_study` | Strong's lexicon, usage, frequency |
| 6 | Morphology / Interlinear | `morphology` | Per-word parsing, interlinear view |
| 7 | Passage Guide | `passage_guide` | Aggregated study data for a passage |
| 8 | Exegetical Guide | `exegetical_guide` | Scholarly exegesis tools |
| 9 | Syntax Search | `syntax_search` | Original-language grammatical search |

### 3.3 Resources

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 10 | Resource Library | `resource_library` | Commentary, dictionary browser |
| 11 | Knowledge Graph | `knowledge_graph` | Visual relationship explorer |

### 3.4 Writing

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 12 | Note Editor | `note_editor` | Rich text notes linked to verses |
| 13 | Sermon Editor | `sermon_editor` | Structured sermon composition |

### 3.5 Tools

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 14 | Search | `search` | Full-text search with FTS5 |
| 15 | Highlights | `highlights` | Highlight and annotation manager |
| 16 | Bookmarks | `bookmarks` | Bookmark manager with folders |
| 17 | Timeline | `timeline` | Biblical chronology visualization |
| 18 | Theological Atlas | `theological_atlas` | Geographic map with biblical locations |
| 19 | Reading Plans | `reading_plans` | Structured reading schedules |

### 3.6 Media

| # | Module | Type Key | Description |
|---|--------|----------|-------------|
| 20 | Audio Sync | `audio_sync` | Synchronized audio Bible playback |
| 21 | Settings | `settings` | App configuration and preferences |

---

## 4. Verse Bus

### 4.1 Purpose

The Verse Bus is the primary mechanism for cross-pane communication. When a user selects a verse in any pane, the Verse Bus broadcasts a `LinkEvent` that all interested panes can react to.

### 4.2 Implementation

```kotlin
class VerseBus {
    private val _events = MutableSharedFlow<LinkEvent>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Flow of navigation events. SharedFlow with replay=1 delivers
     *  the last event to new collectors, so a newly opened pane
     *  immediately receives the current verse context.
     */
    val events: SharedFlow<LinkEvent> = _events.asSharedFlow()

    /** The most recently published event, or null if none. */
    val current: LinkEvent?
        get() = _events.replayCache.firstOrNull()

    /** Publish a navigation event to all collectors. */
    fun navigate(event: LinkEvent) {
        _events.tryEmit(event)
    }
}
```

### 4.3 LinkEvent

```kotlin
sealed class LinkEvent {
    /** Navigate to a specific verse. */
    data class VerseSelected(val globalVerseId: Int) : LinkEvent()

    /** Navigate to a Strong's number (word study). */
    data class StrongsSelected(val strongsNumber: String) : LinkEvent()

    /** Navigate to a passage range. */
    data class PassageSelected(val startVerseId: Int, val endVerseId: Int) : LinkEvent()

    /** Navigate to a resource entry. */
    data class ResourceSelected(val resourceId: String, val entryId: String) : LinkEvent()

    /** Navigate to a search result. */
    data class SearchResult(val query: String, val globalVerseId: Int) : LinkEvent()
}
```

### 4.4 Subscription Pattern

Decompose components collect events from the Verse Bus using `coroutineScope`:

```kotlin
class DefaultCrossReferencesComponent(
    componentContext: ComponentContext,
    private val repository: CrossRefRepository,
    private val verseBus: VerseBus,
) : CrossReferencesComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(CrossRefState())
    override val state: StateFlow<CrossRefState> = _state.asStateFlow()

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    init {
        // React to Verse Bus events
        scope.launch {
            verseBus.events.collect { event ->
                when (event) {
                    is LinkEvent.VerseSelected -> loadReferences(event.globalVerseId)
                    else -> { /* ignore other event types */ }
                }
            }
        }
    }

    private fun loadReferences(globalVerseId: Int) {
        scope.launch {
            _state.update { it.copy(loading = true) }
            repository.getReferencesForVerse(globalVerseId)
                .onSuccess { refs ->
                    _state.update { it.copy(loading = false, references = refs) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.toAppError()) }
                }
        }
    }
}
```

### 4.5 Event Flow Diagram

```
┌─────────────┐    LinkEvent     ┌──────────┐
│ Bible Reader ├────────────────►│          │
│ (publisher)  │                 │  Verse   │
└─────────────┘                 │   Bus    │
                                │          │
┌─────────────┐                 │  Shared  │
│  Cross-Refs  │◄───────────────┤  Flow    │
│ (collector)  │                │          │
├─────────────┤                 │  Replays │
│  Word Study  │◄───────────────┤   last   │
│ (collector)  │                │  event   │
├─────────────┤                 │          │
│  Morphology  │◄───────────────┤          │
│ (collector)  │                └──────────┘
└─────────────┘
```

---

## 5. Workspace Layout

### 5.1 LayoutNode

The workspace layout is represented as an immutable tree of `LayoutNode` objects using Kotlin sealed classes:

```kotlin
sealed class LayoutNode {
    /** A split dividing space between two children. */
    data class Split(
        val axis: SplitAxis,
        val ratio: Float,               // 0.0–1.0, position of divider
        val first: LayoutNode,
        val second: LayoutNode,
    ) : LayoutNode()

    /** A single pane displaying one module. */
    data class Leaf(
        val paneType: String,
        val config: Map<String, String> = emptyMap(),
    ) : LayoutNode()

    /** A tab group containing multiple panes, one active. */
    data class Tabs(
        val children: List<Leaf>,
        val activeIndex: Int,
    ) : LayoutNode()
}

enum class SplitAxis { Horizontal, Vertical }
```

### 5.2 Layout Examples

**Two-pane horizontal split:**
```kotlin
LayoutNode.Split(
    axis = SplitAxis.Horizontal,
    ratio = 0.5f,
    first = LayoutNode.Leaf(paneType = "bible_reader"),
    second = LayoutNode.Leaf(paneType = "cross_references"),
)
```

**Three-pane layout (Bible + tabs):**
```kotlin
LayoutNode.Split(
    axis = SplitAxis.Horizontal,
    ratio = 0.6f,
    first = LayoutNode.Leaf(paneType = "bible_reader"),
    second = LayoutNode.Tabs(
        children = listOf(
            LayoutNode.Leaf(paneType = "cross_references"),
            LayoutNode.Leaf(paneType = "word_study"),
            LayoutNode.Leaf(paneType = "morphology"),
        ),
        activeIndex = 0,
    ),
)
```

### 5.3 Workspace Presets

| Preset | Description | Layout |
|--------|-------------|--------|
| **Reading** | Full-screen Bible reader | Single `Leaf("bible_reader")` |
| **Study** | Bible + study tools | H-split: reader (60%) \| tabs(cross-refs, word study, morphology) |
| **Comparison** | Side-by-side versions | H-split: reader (50%) \| text_comparison (50%) |
| **Sermon Prep** | Bible + notes + sermon | H-split: reader (40%) \| V-split: notes (50%) / sermon (50%) |
| **Research** | Bible + resources + notes | H-split: reader (40%) \| V-split: resource_library (60%) / notes (40%) |

### 5.4 JSON Persistence

Workspace layouts are serialized to JSON via `kotlinx.serialization` and stored in `workspace_layouts` via `SettingsQueries`:

```json
{
  "type": "split",
  "axis": "horizontal",
  "ratio": 0.6,
  "first": {
    "type": "leaf",
    "paneType": "bible_reader",
    "config": { "bibleId": "1", "bookId": "1", "chapter": "1" }
  },
  "second": {
    "type": "tabs",
    "activeIndex": 0,
    "children": [
      { "type": "leaf", "paneType": "cross_references" },
      { "type": "leaf", "paneType": "word_study" }
    ]
  }
}
```

```kotlin
@Serializable
sealed class LayoutNodeDto {
    @Serializable
    @SerialName("split")
    data class Split(
        val axis: String,
        val ratio: Float,
        val first: LayoutNodeDto,
        val second: LayoutNodeDto,
    ) : LayoutNodeDto()

    @Serializable
    @SerialName("leaf")
    data class Leaf(
        val paneType: String,
        val config: Map<String, String> = emptyMap(),
    ) : LayoutNodeDto()

    @Serializable
    @SerialName("tabs")
    data class Tabs(
        val children: List<Leaf>,
        val activeIndex: Int,
    ) : LayoutNodeDto()
}
```

### 5.5 WorkspaceComponent

```kotlin
interface WorkspaceComponent {
    val state: StateFlow<WorkspaceState>

    fun loadWorkspace(workspaceId: String)
    fun updateLayout(layout: LayoutNode)
    fun addPane(paneType: String)
    fun removePane(paneType: String)
    fun resizeSplit(path: List<Int>, newRatio: Float)
    fun movePane(from: List<Int>, to: List<Int>)
    fun switchTab(path: List<Int>, index: Int)
    fun applyPreset(preset: WorkspacePreset)
    fun saveWorkspace()
}

data class WorkspaceState(
    val layout: LayoutNode = LayoutNode.Leaf(paneType = "bible_reader"),
    val workspaceName: String = "Default",
    val loading: Boolean = false,
    val error: AppError? = null,
)
```

### 5.6 Layout Composable Rendering

```kotlin
@Composable
fun LayoutNodeRenderer(
    node: LayoutNode,
    paneRegistry: PaneRegistry,
    modifier: Modifier = Modifier,
) {
    when (node) {
        is LayoutNode.Leaf -> {
            PaneContainer(
                metadata = paneRegistry.metadataFor(node.paneType),
                onClose = { /* dispatch RemovePane */ },
            ) {
                paneRegistry.Build(node.paneType, node.config)
            }
        }
        is LayoutNode.Split -> {
            SplitPane(
                axis = node.axis,
                ratio = node.ratio,
                onResize = { newRatio -> /* dispatch ResizeSplit */ },
            ) {
                first { LayoutNodeRenderer(node.first, paneRegistry) }
                second { LayoutNodeRenderer(node.second, paneRegistry) }
            }
        }
        is LayoutNode.Tabs -> {
            TabGroupPane(
                tabs = node.children,
                activeIndex = node.activeIndex,
                onTabSelected = { index -> /* dispatch SwitchTab */ },
            ) { leaf ->
                paneRegistry.Build(leaf.paneType, leaf.config)
            }
        }
    }
}
```

---

## 6. Module Creation Guide

This section walks through creating a new module from scratch, using "**Parables Explorer**" as an example.

### Step 1: Define the Pane Type

Choose a unique type key following the `snake_case` convention:

```
parables_explorer
```

### Step 2: Create the Feature Directory

```
shared/src/commonMain/kotlin/org/biblestudio/features/parables_explorer/
├── domain/
│   ├── entities/
│   │   └── Parable.kt
│   └── repositories/
│       └── ParableRepository.kt
├── data/
│   ├── repositories/
│   │   └── ParableRepositoryImpl.kt
│   └── mappers/
│       └── ParableMappers.kt
└── presentation/
    ├── components/
    │   ├── ParablesComponent.kt           # Interface
    │   └── DefaultParablesComponent.kt    # Implementation
    ├── content/
    │   └── ParablesContent.kt             # Root @Composable
    └── composables/
        ├── ParableCard.kt
        └── ParableDetail.kt
```

### Step 3: Define the Entity

```kotlin
// domain/entities/Parable.kt
data class Parable(
    val id: Int,
    val title: String,
    val startVerseId: Int,
    val endVerseId: Int,
    val theme: String,
    val summary: String,
)
```

### Step 4: Define the Repository Interface

```kotlin
// domain/repositories/ParableRepository.kt
interface ParableRepository {
    suspend fun getAllParables(): Result<List<Parable>>
    suspend fun getParablesByTheme(theme: String): Result<List<Parable>>
    suspend fun getParableForVerse(globalVerseId: Int): Result<Parable?>
}
```

### Step 5: Implement the Repository

```kotlin
// data/repositories/ParableRepositoryImpl.kt
class ParableRepositoryImpl(
    private val database: BibleStudioDatabase,
) : ParableRepository {

    override suspend fun getAllParables(): Result<List<Parable>> = runCatching {
        database.referenceQueries
            .allParables()
            .executeAsList()
            .map { it.toParable() }
    }

    // ... other methods
}
```

### Step 6: Create the Component

```kotlin
// presentation/components/ParablesComponent.kt
interface ParablesComponent {
    val state: StateFlow<ParablesState>

    fun loadAll()
    fun filterByTheme(theme: String)
    fun selectParable(parable: Parable)
}

data class ParablesState(
    val loading: Boolean = false,
    val parables: List<Parable> = emptyList(),
    val selectedParable: Parable? = null,
    val error: AppError? = null,
)
```

```kotlin
// presentation/components/DefaultParablesComponent.kt
class DefaultParablesComponent(
    componentContext: ComponentContext,
    private val repository: ParableRepository,
    private val verseBus: VerseBus,
) : ParablesComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(ParablesState())
    override val state: StateFlow<ParablesState> = _state.asStateFlow()

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    init {
        // React to Verse Bus events
        scope.launch {
            verseBus.events.collect { event ->
                if (event is LinkEvent.VerseSelected) {
                    // Auto-show the parable containing this verse
                    loadParableForVerse(event.globalVerseId)
                }
            }
        }
    }

    override fun loadAll() {
        scope.launch {
            _state.update { it.copy(loading = true) }
            repository.getAllParables()
                .onSuccess { parables ->
                    _state.update { it.copy(loading = false, parables = parables) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.toAppError()) }
                }
        }
    }

    override fun selectParable(parable: Parable) {
        _state.update { it.copy(selectedParable = parable) }
        // Publish to Verse Bus so other panes navigate to the parable's passage
        verseBus.navigate(LinkEvent.PassageSelected(parable.startVerseId, parable.endVerseId))
    }

    // ... other methods
}
```

### Step 7: Build the Pane Composable

```kotlin
// presentation/content/ParablesContent.kt
@Composable
fun ParablesPane(config: Map<String, String>) {
    // Component created via Koin or Decompose child
    val component = remember { getKoin().get<ParablesComponent>() }
    val state by component.state.collectAsState()

    LaunchedEffect(Unit) {
        component.loadAll()
    }

    when {
        state.loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!.userMessage)
            }
        }
        state.selectedParable != null -> {
            ParableDetail(
                parable = state.selectedParable!!,
                onBack = { component.clearSelection() },
            )
        }
        else -> {
            ParablesList(
                parables = state.parables,
                onSelect = { component.selectParable(it) },
            )
        }
    }
}
```

### Step 8: Register in PaneRegistry

Add the registration call in `PaneRegistry.init()`:

```kotlin
register("parables_explorer") { config -> ParablesPane(config) }
```

### Step 9: Add Metadata

```kotlin
val PARABLES_META = PaneMetadata(
    type = "parables_explorer",
    displayName = "Parables",
    icon = Icons.AutoMirrored.Rounded.MenuBook,
    category = PaneCategory.Study,
    description = "Explore the parables of Jesus",
)
```

### Step 10: Register in Koin

```kotlin
// di/Modules.kt
val parablesModule = module {
    single<ParableRepository> { ParableRepositoryImpl(get()) }
    factory { params ->
        DefaultParablesComponent(
            componentContext = params.get(),
            repository = get(),
            verseBus = get(),
        )
    }
}
```

### Step 11: Create Module Documentation

Create `doc/modules/parables-explorer/` with the standard 7 files:
- `README.md`, `DATA_MODEL.md`, `UI_SPEC.md`, `EVENTS.md`, `API.md`, `TESTING.md`, `CHANGELOG.md`

See the template at `doc/modules/_template/`.

### Step 12: Add to Workspace Preset (Optional)

If the module should appear in a default workspace preset, update the preset definitions in `WorkspaceComponent`.

---

## 7. Boot-to-Render Flow

Complete lifecycle from app launch to first pane render:

```
main() / Application.onCreate() / ContentView
  → initKoin()
    → createSqlDriver() via expect/actual
    → BibleStudioDatabase(driver) — runs pending migrations
    → Register Koin singletons (database, VerseBus, PaneRegistry)
    → Register repository implementations
    → Register component factories
    → PaneRegistry.init() registers 21+ builders
  → Compose entry point
    → AppTheme { AdaptiveShell(rootComponent) }
      → RootComponent creates WorkspaceComponent via Decompose childStack
        → WorkspaceComponent loads workspace from SettingsRepository
          → Deserialize JSON → LayoutNode tree
        → WorkspaceComponent emits WorkspaceState(layout = tree)
        → AdaptiveShell renders LayoutNode tree:
          → LayoutNode.Split → Row/Column with draggable divider
          → LayoutNode.Tabs → TabRow + content switching
          → LayoutNode.Leaf → PaneContainer wrapping paneRegistry.Build(type)
            → Pane composable creates Component via Koin
            → Component launches initial load
            → Component collects VerseBus events
            → First state emitted → pane content recomposes
```

---

## 8. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, DI, bootstrap |
| [DATA_LAYER.md](DATA_LAYER.md) | SettingsQueries, workspace_layouts table |
| [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) | PaneContainer component, pane category colors |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | Component pattern, naming rules |
| [doc/modules/_template/](modules/_template/) | Module documentation template |
