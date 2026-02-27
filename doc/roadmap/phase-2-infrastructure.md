# Phase 2 — Infrastructure

> Core infrastructure: VerseBus, PaneRegistry, Decompose routing, AppTheme, WorkspaceShell.
> **Prerequisites**: Phase 1 complete (Gradle builds, SQLDelight schema, Koin DI, app shell).

---

## 2.1 Verse Bus

- [x] Create `LinkEvent` sealed class with 5 variants: `VerseSelected`, `StrongsSelected`, `PassageSelected`, `ResourceSelected`, `SearchResult`
- [x] Create `VerseBus` class with `MutableSharedFlow<LinkEvent>(replay = 1)`
- [x] Add `publish(event: LinkEvent)` and `val events: SharedFlow<LinkEvent>` API
- [x] Register `VerseBus` as singleton in Koin `coreModule`
- [x] Write test: published event is received by collector
- [x] Write test: new subscriber receives last replayed event
- [x] Write test: multiple subscribers all receive events (fan-out)

## 2.2 PaneRegistry

- [x] Create `PaneRegistry` object with `register(key: String, builder: PaneBuilder)` API
- [x] Define `PaneBuilder` type alias: `@Composable (config: Map<String, String>) -> Unit`
- [x] Create `PaneMetadata` data class: `type`, `displayName`, `icon`, `category`, `description`
- [x] Create `PaneCategory` enum: `Text`, `Study`, `Resource`, `Writing`, `Tool`, `Media`
- [x] Implement `build(type: String, config: Map<String, String>)` with `IllegalArgumentException` for unknown types
- [x] Expose `availableTypes: Set<String>` and `metadata(type: String): PaneMetadata`
- [x] Create `PaneRegistry.init()` that registers placeholder builders for all 21 pane types
- [x] Write test: all 21 types are registered after `init()`
- [x] Write test: `build()` throws for unknown type

## 2.3 Decompose Root Navigation

- [x] Create `RootConfig` sealed class: `Workspace`, `Settings`, `Import`, `DeepLink(globalVerseId: Long)`
- [x] Create `RootComponent` interface with `childStack: Value<ChildStack<RootConfig, RootChild>>`
- [x] Implement `DefaultRootComponent` using `StackNavigation<RootConfig>`
- [x] Implement `createChild()` factory method for each config type
- [x] Add `navigateTo(config: RootConfig)` method
- [x] Wire deep link resolution: `DeepLink(id)` → publish to VerseBus → navigate to `Workspace`
- [x] Write test: child stack starts with `Workspace` config
- [x] Write test: navigation pushes new child onto stack
- [x] Write test: deep link publishes to VerseBus and navigates to Workspace

## 2.4 LayoutNode & Workspace Engine

- [x] Create `LayoutNode` sealed class: `Split(axis, ratio, first, second)`, `Leaf(paneType, config)`, `Tabs(children, activeIndex)`
- [x] Implement `LayoutNode` serialization/deserialization with `kotlinx.serialization`
- [x] Create `WorkspaceComponent` interface that manages a `StateFlow<WorkspaceState>`
- [x] Implement `addPane(type)` — inserts a pane into layout tree
- [x] Implement `removePane(type)` — removes and rebalances
- [x] Implement `movePane(from, to)` — path-based rearrangement
- [x] Create 5 workspace presets: `Default`, `Study`, `Exegesis`, `Writing`, `Research`
- [x] Persist active layout to `workspace_layouts` table via `WorkspaceRepository` (debounced 2s auto-save)
- [x] Restore last layout on `loadWorkspace()`
- [x] Write test: serialization round-trips LayoutNode correctly
- [x] Write test: preset layouts contain expected pane types (Study, Research, Default)

## 2.5 WorkspaceShell Composable

- [x] Create `WorkspaceShell` composable — activity bar (side rail) + content area
- [x] Implement activity bar with icons for core pane types
- [x] Implement pane rendering: recursively walk `LayoutNode` tree → `Row {}`, `Column {}`, tab strip
- [x] Implement `PaneContainer` placeholder with pane type label
- [x] Implement pane header bar: title, icon, close button, overflow menu
- [x] Implement drag handles between panes for resizing (desktop)
- [x] Implement "Add Pane" button/menu with all available pane types
- [x] Wire pane close to `WorkspaceComponent.removePane()`
- [x] Wire pane header overflow to split/move/pin actions
- [x] Write UI test: workspace renders a single-pane layout
- [x] Write UI test: workspace renders a split-pane layout

## 2.6 AppTheme

- [x] Create `AppTheme` composable wrapping `MaterialTheme` with custom `ColorScheme`
- [x] Implement light color scheme from DESIGN_SYSTEM §2.1 tokens
- [x] Implement dark color scheme from DESIGN_SYSTEM §2.1 tokens
- [x] Create custom `Typography` with 10 text styles (DESIGN_SYSTEM §3)
- [x] Create pane category accent colors (6 colors from DESIGN_SYSTEM §2.2)
- [x] Create `Shapes` definition for cards, dialogs, bottom sheets
- [x] Wire `isSystemInDarkTheme()` detection in `AppTheme`
- [x] Create `AppColors` object with highlight palette (8 colors) and semantic colors
- [x] Create `Spacing` object with 8 spacing tokens
- [x] Create `AnimationDurations` object
- [x] Create `AppIcons` object with custom `ImageVector` icons (Scripture Book, Cross-Ref, Strong's, etc.)
- [x] Write UI test: theme applies correct colors in light mode
- [x] Write UI test: theme applies correct colors in dark mode

## 2.7 Responsive Layout

- [x] Create `WindowSizeClass` enum: `Compact` (<600dp), `Medium` (600–839dp), `Expanded` (840–1199dp), `Large` (≥1200dp)
- [x] Implement `AdaptiveShell` composable with `BoxWithConstraints` for adaptive content switching
- [x] Implement adaptive shell: bottom nav (Compact), navigation rail (Medium), side rail + workspace (Expanded)
- [x] Write UI test: compact layout shows bottom navigation
- [x] Write UI test: expanded layout shows side rail + workspace

## 2.8 i18n Foundation

- [x] Set up `composeResources/values/strings.xml` for English strings
- [x] Set up `composeResources/values-es/strings.xml` for Spanish strings
- [x] Add core UI strings: navigation labels, pane titles, common actions (Save, Cancel, Delete, Search)
- [x] Create `stringResource()` wrapper for portable access
- [x] Write test: English and Spanish strings resolve correctly

---

## Phase 2 Exit Criteria

- [x] VerseBus sends/receives events across components
- [x] PaneRegistry has 21 types registered with metadata
- [x] Decompose root navigation works (Workspace ↔ Settings ↔ Import)
- [x] WorkspaceShell renders multi-pane layouts from LayoutNode tree
- [x] AppTheme applies light/dark palettes correctly
- [x] Responsive layout adapts to mobile/tablet/desktop breakpoints
- [x] i18n loads EN + ES strings
- [x] All infrastructure tests pass
