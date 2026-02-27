# Search — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

### 1.1 Configuration Table

| Config | Parameters | Description |
|--------|-----------|-------------|
| `SearchConfig.Main` | — | Open search pane with empty query |
| `SearchConfig.Query(query)` | `query: String` | Open search pane with pre-filled query |

### 1.2 Config Definition

```kotlin
sealed class SearchConfig : Parcelable {
    @Parcelize data object Main : SearchConfig()
    @Parcelize data class Query(val query: String) : SearchConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `bible-reader` | via VerseBus `SearchResult` | `globalVerseId: Int` | When user taps a search result |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("search", config = mapOf("query" to "God created"))
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | `String` | No | Pre-fill search query |

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://search/{query}` | `biblestudio://search/God%20created` | Opens search with pre-filled query |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Publisher** | Publishes `LinkEvent.SearchResult(globalVerseId)` when user taps a result |

### 5.1 Publishing

```kotlin
// User taps a search result
verseBus.publish(LinkEvent.SearchResult(globalVerseId = result.globalVerseId))
```

---

## 6. Inter-Module Communication Diagram

```
┌──────────────┐  SearchResult(verseId)  ┌──────────────┐
│   Search     │ ───────────────────────→ │ Bible Reader │
└──────────────┘                          └──────────────┘
       │                                         │
       │ queries fts_verses, fts_notes, etc.     │
       ▼                                         │
  ┌──────────┐                                   │
  │ FTS5 DBs │ ←─────────────────────────────────┘
  └──────────┘   (verses, notes, resources tables)
```
