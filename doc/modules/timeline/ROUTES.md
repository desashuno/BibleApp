# Timeline — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `TimelineConfig.Main` | — | Full timeline view |
| `TimelineConfig.Era(era)` | `era: String` | Filtered to specific era |
| `TimelineConfig.Event(id)` | `eventId: Long` | Focused on specific event |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to event's verse |
| `knowledge-graph` | `KnowledgeGraphConfig.Entity(id)` | View event entity |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://timeline` | — | Opens timeline |
| `biblestudio://timeline/era/{era}` | `biblestudio://timeline/era/Patriarchs` | Filtered view |
| `biblestudio://timeline/event/{id}` | `biblestudio://timeline/event/42` | Event detail |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes on verse ref tap |
