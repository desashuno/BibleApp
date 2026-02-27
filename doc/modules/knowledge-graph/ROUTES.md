# Knowledge Graph — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `KnowledgeGraphConfig.Main` | — | Entity browser |
| `KnowledgeGraphConfig.Entity(id)` | `entityId: Long` | Entity detail + graph |
| `KnowledgeGraphConfig.ForVerse(verseId)` | `globalVerseId: Int` | Entities in a verse |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to verse reference |
| `theological-atlas` | `AtlasConfig.Location(id)` | Show place on map |
| `timeline` | `TimelineConfig.Event(id)` | Show event on timeline |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://knowledge-graph` | — | Opens entity browser |
| `biblestudio://knowledge-graph/entity/{id}` | `biblestudio://knowledge-graph/entity/42` | Entity detail |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes on verse ref tap |
