# Theological Atlas — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  TheologicalAtlasPane                             |
|  MapView / LocationDetail / LocationList          |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultAtlasComponent (Decompose)                |
|  +-- Manages StateFlow<AtlasState>                |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Queries AtlasRepository                      |
+---------------------------------------------------+
|                      DATA                         |
|  AtlasRepository (interface)                      |
|  AtlasRepositoryImpl                              |
|  +-- AtlasQueries (SQLDelight)                    |
|       +-- SQLite (geographic_locations)           |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Browse Map

1. **User opens pane** — Loads map tiles + location markers.
2. **Marker tap** — Shows location detail with verse references.
3. **Verse link** — Publishes `VerseSelected` on VerseBus.

### 2.2 Secondary Flows

- **VerseBus** — On `VerseSelected`, highlights locations mentioned in the verse.
- **List view** — Alphabetical location list as alternative to map.
- **Search** — Filter locations by name.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Atlas.sq` | `allLocations` | — | `List<Location>` | All locations |
| `Atlas.sq` | `locationById` | `id: Long` | `Location?` | Single location |
| `Atlas.sq` | `locationsForVerse` | `globalVerseId: Int` | `List<Location>` | Locations in verse |
| `Atlas.sq` | `searchLocations` | `query: String` | `List<Location>` | Name search |

---

## 4. Dependency Injection

```kotlin
val atlasModule = module {
    singleOf(::AtlasRepositoryImpl) bind AtlasRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultAtlasComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `AtlasRepositoryImpl` | Abstracts location queries |
| Observer | VerseBus subscriber | Auto-highlights verse locations |
| Tile layer | Map rendering | Efficient large-scale map display |

---

## 6. Performance Considerations

- **Location query < 5 ms** — Indexed on lat/lon and `global_verse_id`.
- **Map tiles** — Bundled low-res; higher-res downloaded on demand.
- **Marker clustering** — Groups nearby markers at zoom-out levels.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Bundled map tiles | Online-only maps | Offline-first for Bible study anywhere |
| Pre-populated locations | User-entered | Biblical geography is static reference data |
| Compose Canvas map | WebView + Leaflet | Native rendering; no WebView dependency |
