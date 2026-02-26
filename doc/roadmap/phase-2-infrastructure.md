# Phase 2 — Infrastructure

> Core infrastructure: VerseBus, PaneRegistry, Decompose routing, AppTheme, WorkspaceShell.
> **Prerequisites**: Phase 1 complete (Gradle builds, SQLDelight schema, Koin DI, app shell).

---

## 2.1 Verse Bus

- [ ] Create `LinkEvent` sealed class with 5 variants: `VerseSelected`, `StrongsSelected`, `PassageSelected`, `ResourceSelected`, `SearchResult`
- [ ] Create `VerseBus` class with `MutableSharedFlow<LinkEvent>(replay = 1)`
- [ ] Add `publish(event: LinkEvent)` and `val events: SharedFlow<LinkEvent>` API
- [ ] Register `VerseBus` as singleton in Koin `coreModule`
- [ ] Write test: published event is received by collector
- [ ] Write test: new subscriber receives last replayed event
- [ ] Write test: multiple subscribers all receive events (fan-out)

## 2.2 PaneRegistry

- [ ] Create `PaneRegistry` object with `register(key: String, builder: PaneBuilder)` API
- [ ] Define `PaneBuilder` type alias: `@Composable (config: Map<String, String>) -> Unit`
- [ ] Create `PaneMetadata` data class: `type`, `displayName`, `icon`, `category`, `description`
- [ ] Create `PaneCategory` enum: `Text`, `Study`, `Resource`, `Writing`, `Tool`, `Media`
- [ ] Implement `build(type: String, config: Map<String, String>)` with `IllegalArgumentException` for unknown types
- [ ] Expose `availableTypes: Set<String>` and `metadata(type: String): PaneMetadata`
- [ ] Create `PaneRegistry.init()` that registers placeholder builders for all 21 pane types
- [ ] Write test: all 21 types are registered after `init()`
- [ ] Write test: `build()` throws for unknown type

## 2.3 Decompose Root Navigation

- [ ] Create `RootConfig` sealed class: `Workspace`, `Settings`, `Import`, `DeepLink(globalVerseId: Long)`
- [ ] Create `RootComponent` interface with `childStack: Value<ChildStack<RootConfig, RootChild>>`
- [ ] Implement `DefaultRootComponent` using `StackNavigation<RootConfig>`
- [ ] Implement `createChild()` factory method for each config type
- [ ] Add `navigateTo(config: RootConfig)` method
- [ ] Wire deep link resolution: `DeepLink(id)` → publish to VerseBus → navigate to `Workspace`
- [ ] Write test: child stack starts with `Workspace` config
- [ ] Write test: navigation pushes new child onto stack
- [ ] Write test: deep link publishes to VerseBus and navigates to Workspace

## 2.4 LayoutNode & Workspace Engine

- [ ] Create `LayoutNode` sealed class: `Single(paneType, config)`, `Row(children, weights)`, `Column(children, weights)`, `Tabbed(children, activeIndex)`
- [ ] Implement `LayoutNode` serialization/deserialization with `kotlinx.serialization`
- [ ] Create `WorkspaceComponent` that manages a `StateFlow<LayoutNode>`
- [ ] Implement `addPane(type, config, position)` — inserts a pane into layout tree
- [ ] Implement `removePane(id)` — removes and rebalances
- [ ] Implement `movePane(from, to)` — drag-and-drop rearrangement
- [ ] Create 5 workspace presets: `Default` (Reader only), `Study` (Reader + Cross-Refs + Word Study), `Exegesis` (Reader + Morphology + Passage Guide), `Writing` (Reader + Notes + Sermon), `Research` (Reader + Search + Resources + Knowledge Graph)
- [ ] Persist active layout to `workspace_layouts` table via `SettingsRepository`
- [ ] Restore last layout on app launch
- [ ] Write test: addPane creates correct LayoutNode structure
- [ ] Write test: serialization round-trips LayoutNode correctly
- [ ] Write test: preset layouts contain expected pane types

## 2.5 WorkspaceShell Composable

- [ ] Create `WorkspaceShell` composable — activity bar (side rail) + content area
- [ ] Implement activity bar with icons for all pane categories
- [ ] Implement pane rendering: recursively walk `LayoutNode` tree → `Row {}`, `Column {}`, tab strip
- [ ] Implement pane header bar: title, icon, close button, overflow menu
- [ ] Implement drag handles between panes for resizing (desktop)
- [ ] Implement "Add Pane" button/menu with all available pane types
- [ ] Wire pane close to `WorkspaceComponent.removePane()`
- [ ] Wire pane header overflow to split/move/pin actions
- [ ] Write UI test: workspace renders a single-pane layout
- [ ] Write UI test: workspace renders a split-pane layout

## 2.6 AppTheme

- [ ] Create `AppTheme` composable wrapping `MaterialTheme` with custom `ColorScheme`
- [ ] Implement light color scheme from DESIGN_SYSTEM §2.1 tokens
- [ ] Implement dark color scheme from DESIGN_SYSTEM §2.1 tokens
- [ ] Create custom `Typography` with 12 text styles (DESIGN_SYSTEM §3)
- [ ] Create pane category accent colors (6 colors from DESIGN_SYSTEM §2.2)
- [ ] Create `Shapes` definition for cards, dialogs, bottom sheets
- [ ] Implement `isSystemInDarkTheme()` detection with user override capability
- [ ] Create `AppIcons` object with custom `ImageVector` icons (Scripture Book, Cross-Ref, Strong's, etc.)
- [ ] Write UI test: theme applies correct colors in light mode
- [ ] Write UI test: theme applies correct colors in dark mode

## 2.7 Responsive Layout

- [ ] Create `WindowSizeClass` resolver: `Compact` (0–599dp), `Medium` (600–899dp), `Expanded` (900dp+)
- [ ] Implement adaptive shell: bottom nav (Compact), navigation rail (Medium), side rail + workspace (Expanded)
- [ ] Create `BoxWithConstraints` wrappers for adaptive content switching
- [ ] Write UI test: compact layout shows bottom navigation
- [ ] Write UI test: expanded layout shows side rail + workspace

## 2.8 i18n Foundation

- [ ] Set up `composeResources/values/strings.xml` for English strings
- [ ] Set up `composeResources/values-es/strings.xml` for Spanish strings
- [ ] Create `stringResource()` wrapper for portable access
- [ ] Add core UI strings: navigation labels, pane titles, common actions (Save, Cancel, Delete, Search)
- [ ] Write test: English and Spanish strings resolve correctly

---

## Phase 2 Exit Criteria

- [ ] VerseBus sends/receives events across components
- [ ] PaneRegistry has 21 types registered with metadata
- [ ] Decompose root navigation works (Workspace ↔ Settings ↔ Import)
- [ ] WorkspaceShell renders multi-pane layouts from LayoutNode tree
- [ ] AppTheme applies light/dark palettes correctly
- [ ] Responsive layout adapts to mobile/tablet/desktop breakpoints
- [ ] i18n loads EN + ES strings
- [ ] All infrastructure tests pass
