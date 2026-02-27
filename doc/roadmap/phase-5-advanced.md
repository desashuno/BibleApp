# Phase 5 — Advanced Features

> Knowledge Graph, Timeline, Theological Atlas, Exegetical Guide, Sermon Editor, Reading Plans, Dashboard, Audio Sync.
> **Prerequisites**: Phase 4 complete (Study & Writing modules functional).
> **Note**: Modules in this phase can be developed in parallel by sub-teams.

---

## 5.1 Knowledge Graph — Data Layer

- [ ] Create `KnowledgeGraphRepository` with `getEntity(entityId: Long): GraphEntity`
- [ ] Map `graph_nodes` table to `GraphEntity` data class: `id`, `name`, `type`, `description`, `properties`
- [ ] Map `graph_edges` table to `GraphEdge` data class: `sourceId`, `targetId`, `relationship`, `weight`
- [ ] Implement `getRelated(entityId: Long, depth: Int): GraphCluster` — BFS traversal up to N hops
- [ ] Implement `searchEntities(query: String): List<GraphEntity>` using `graph_fts` FTS5 table
- [ ] Implement entity types: `Person`, `Place`, `Event`, `Concept`, `Book`
- [ ] Implement relationship types: `RelatedTo`, `ChildOf`, `SpouseOf`, `RulerOf`, `LocatedIn`, `MentionedIn`, `AuthorOf`
- [ ] Seed graph with ~2,000 biblical entities and relationships
- [ ] Create `KnowledgeGraphQuery.sq`
- [ ] Register repository in Koin
- [ ] Write test: BFS traversal returns correct cluster at depth 2
- [ ] Write test: FTS search finds entity by partial name

## 5.2 Knowledge Graph — Component & UI

- [ ] Create `KnowledgeGraphComponent` subscribing to VerseBus `VerseSelected`
- [ ] Define `KnowledgeGraphState`: `centerEntity`, `cluster`, `selectedEntity`, `zoomLevel`
- [ ] Implement force-directed graph layout algorithm (Fruchterman-Reingold or similar)
- [ ] Create `KnowledgeGraphPane` composable with `Canvas` drawing:
  - [ ] Draw nodes as circles with category-colored fills
  - [ ] Draw edges as lines with relationship labels
  - [ ] Draw selected-node highlight ring
- [ ] Implement pinch-to-zoom and pan gestures
- [ ] Implement node tap: select entity, show detail card, publish VerseBus event
- [ ] Implement entity detail card: name, type, description, verse references, related entities
- [ ] Implement depth slider (1–4 hops) to expand/collapse graph
- [ ] Register `knowledgeGraph` in `PaneRegistry`
- [ ] Write UI test: canvas renders nodes and edges
- [ ] Write test: layout algorithm produces non-overlapping positions

## 5.3 Timeline — Data Layer

- [ ] Create `TimelineRepository` with `getEvents(startYear: Int, endYear: Int): List<TimelineEvent>`
- [ ] Map `timeline_events` table to `TimelineEvent` data class: `id`, `title`, `description`, `startYear`, `endYear`, `category`, `verseRefs`
- [ ] Implement `TimelineCategory` enum: `Creation`, `Patriarchs`, `Exodus`, `Judges`, `Kingdom`, `Exile`, `Return`, `Intertestamental`, `NewTestament`, `EarlyChurch`
- [ ] Seed timeline with ~200 key biblical events
- [ ] Create `TimelineQuery.sq` with range queries
- [ ] Register repository in Koin
- [ ] Write test: range query returns events within date bounds

## 5.4 Timeline — Component & UI

- [ ] Create `TimelineComponent` subscribing to VerseBus `VerseSelected`
- [ ] Define `TimelineState`: `events`, `visibleRange`, `selectedEvent`, `zoomLevel`
- [ ] Create `TimelinePane` composable with horizontal scrollable `Canvas`
- [ ] Draw era bands with category colors
- [ ] Draw event markers with title labels
- [ ] Implement pinch-to-zoom for time scale (decade / century / millennium)
- [ ] Implement event tap: show detail card with description + verse links
- [ ] Implement event verse link tap: publish `LinkEvent.VerseSelected`
- [ ] Register `timeline` in `PaneRegistry`
- [ ] Write UI test: timeline renders event markers
- [ ] Write test: zoom level correctly scales time axis

## 5.5 Theological Atlas — Data Layer

- [ ] Create `AtlasRepository` with `getLocations(): List<AtlasLocation>`
- [ ] Map `atlas_locations` table to `AtlasLocation` data class: `id`, `name`, `latitude`, `longitude`, `description`, `verseRefs`, `period`
- [ ] Implement `getLocationsForVerse(verseId: Long): List<AtlasLocation>`
- [ ] Implement `getLocationsInBounds(north, south, east, west): List<AtlasLocation>`
- [ ] Seed atlas with ~150 biblical locations
- [ ] Create `AtlasQuery.sq`
- [ ] Register repository in Koin
- [ ] Write test: location query returns points within bounds

## 5.6 Theological Atlas — Component & UI

- [ ] Create `AtlasComponent` subscribing to VerseBus `VerseSelected`
- [ ] Define `AtlasState`: `locations`, `selectedLocation`, `mapCenter`, `zoomLevel`, `activePeriod`
- [ ] Create `AtlasPane` composable with tile-based map renderer (offline tiles)
- [ ] Draw location markers with name labels on map
- [ ] Implement marker tap: show location detail card with description + verse refs
- [ ] Implement period filter: show/hide locations by historical period
- [ ] Implement journey overlay: draw path lines for patriarchal journeys, Exodus route, Paul's missions
- [ ] Register `theologicalAtlas` in `PaneRegistry`
- [ ] Write UI test: map renders markers at correct positions
- [ ] Write test: period filter correctly shows/hides locations

## 5.7 Exegetical Guide

- [x] Create `ExegeticalGuideRepository` aggregating cross-refs, morphology, word study, outlines, and commentaries
- [ ] Implement `getGuide(verseId: Long): ExegeticalGuide` — structured analysis object
- [ ] Define `ExegeticalGuide` data class with sections: `textCritical`, `grammatical`, `lexical`, `structural`, `theological`, `applicational`
- [x] Create `ExegeticalGuideComponent` subscribing to VerseBus `VerseSelected`
- [x] Create `ExegeticalGuidePane` composable: sectioned scrollable analysis
- [ ] Implement text-critical section: manuscript variant readings (if data available)
- [ ] Implement grammatical section: parsed morphology with syntactic annotations
- [x] Implement lexical section: key word definitions with semantic range
- [ ] Implement structural section: passage outline with chiastic / rhetorical structure
- [x] Implement theological section: cross-references + doctrinal themes
- [x] Register `exegeticalGuide` in `PaneRegistry`
- [x] Write test: guide aggregates all sections correctly
- [ ] Write UI test: all 6 sections render

## 5.8 Sermon Editor

- [x] Create `SermonRepository` with `getSermons(): List<Sermon>`, `saveSermon()`, `deleteSermon()`
- [x] Map `sermons` table to `Sermon` data class: `id`, `title`, `date`, `passage`, `outline`, `content`, `notes`, `status`
- [x] Implement `SermonStatus` enum: `Draft`, `InProgress`, `Ready`, `Delivered`
- [x] Implement sermon search using `sermons_fts` FTS5 table
- [x] Create `SermonEditorComponent` with `StateFlow<SermonEditorState>`
- [x] Define `SermonEditorState`: `sermon`, `isDirty`, `wordCount`, `estimatedMinutes`
- [x] Create `SermonEditorPane` composable: metadata header + Markdown editor + Scripture reference inline
- [x] Implement outline mode: drag-and-drop reorderable point list
- [ ] Implement Scripture reference insertion (same as Note Editor but sermon context)
- [x] Implement word count and estimated delivery time (150 wpm)
- [ ] Implement export to PDF / DOCX using platform-specific libraries
- [x] Register `sermonEditor` in `PaneRegistry`
- [x] Write test: sermon CRUD + FTS search
- [ ] Write UI test: sermon editor renders outline mode

## 5.9 Reading Plans — Data Layer

- [x] Create `ReadingPlanRepository` with `getPlans(): List<ReadingPlan>`, `getProgress(planId): ReadingPlanProgress`
- [x] Map `reading_plans` table to `ReadingPlan` data class: `id`, `name`, `description`, `durationDays`, `entries`
- [x] Map `reading_plan_progress` table to `ReadingPlanProgress` data class: `planId`, `dayIndex`, `completed`, `completedAt`
- [ ] Implement built-in plans: "Bible in a Year" (365 days), "NT in 90 Days", "Psalms & Proverbs (31 days)", "Gospels (30 days)"
- [x] Create `ReadingPlanQuery.sq`
- [x] Register repository in Koin
- [x] Write test: plan progress tracks completed days

## 5.10 Reading Plans — Component & UI

- [x] Create `ReadingPlanComponent` with `StateFlow<ReadingPlanState>`
- [x] Define `ReadingPlanState`: `activePlan`, `todayReading`, `progress`, `streakDays`
- [x] Create `ReadingPlanPane` composable: progress ring + today's reading + calendar grid
- [ ] Implement today's reading card: passage reference + "Start Reading" button
- [ ] Implement "Start Reading" tap: publish `LinkEvent.PassageSelected` to VerseBus
- [x] Implement calendar grid with color-coded completion status per day
- [x] Implement streak counter and notification reminder (platform-specific)
- [x] Register `readingPlans` in `PaneRegistry`
- [ ] Write UI test: progress ring shows correct percentage
- [x] Write test: streak counter increments on consecutive days

## 5.11 Dashboard

- [x] Create `DashboardComponent` with `StateFlow<DashboardState>`
- [x] Define `DashboardState`: `recentHistory`, `activeReadingPlan`, `dailyVerse`, `recentNotes`, `quickActions`
- [x] Create `DashboardPane` composable: card grid landing page
- [ ] Implement "Daily Verse" card: random verse with share button
- [ ] Implement "Continue Reading" card: last read passage + resume button
- [ ] Implement "Reading Plan Progress" card: compact progress ring + today's reading
- [ ] Implement "Recent Notes" card: last 3 notes with tap to open
- [ ] Implement "Quick Actions" row: Search, New Note, Bookmarks, Settings
- [x] Register `dashboard` in `PaneRegistry`
- [ ] Write UI test: dashboard renders all cards

## 5.12 Audio Sync

- [ ] Create `AudioRepository` with `getAudioTrack(bookId, chapter): AudioTrack?`
- [ ] Map `audio_tracks` table to `AudioTrack` data class: `id`, `bookId`, `chapter`, `filePath`, `durationMs`, `timingData`
- [ ] Implement `TimingData`: list of `(verseNum, startMs, endMs)` for verse-level sync
- [ ] Create platform-specific audio player: `expect class AudioPlayer` / `actual class`
- [ ] Implement `AudioPlayerComponent` with `StateFlow<AudioPlayerState>`
- [ ] Define `AudioPlayerState`: `isPlaying`, `currentPositionMs`, `durationMs`, `currentVerse`, `playbackSpeed`
- [ ] Implement play/pause/seek controls
- [ ] Implement playback speed: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x
- [ ] Wire verse sync: as audio plays, publish `LinkEvent.VerseSelected` at each verse boundary
- [ ] Wire reverse sync: when user taps verse in Reader, seek audio to that verse's start time
- [ ] Create `AudioPlayerPane` composable: transport controls + progress bar + verse indicator
- [ ] Implement background playback with notification controls (Android/iOS)
- [ ] Register `audioSync` in `PaneRegistry`
- [ ] Write test: timing data correctly maps position to verse
- [ ] Write test: playback speed affects position progression
- [ ] Write UI test: audio controls render play/pause state

---

## Phase 5 Exit Criteria

- [ ] Knowledge Graph renders force-directed entity network with interactive navigation
- [ ] Timeline displays era-colored event markers with zoom capability
- [ ] Theological Atlas shows biblical locations on offline map with period filtering
- [ ] Exegetical Guide generates structured 6-section analysis per verse
- [ ] Sermon Editor supports Markdown + outline mode with export
- [ ] Reading Plans track daily progress with streak counter
- [ ] Dashboard shows personalized landing page with quick actions
- [ ] Audio Sync plays chapter audio with real-time verse highlighting
- [ ] All advanced module tests pass with ≥ 80% coverage
