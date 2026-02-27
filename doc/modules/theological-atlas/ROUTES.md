# Theological Atlas — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `AtlasConfig.Main` | — | Map view |
| `AtlasConfig.Location(id)` | `locationId: Long` | Location detail |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to location's verse |
| `knowledge-graph` | `KnowledgeGraphConfig.Entity(id)` | View place entity |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://atlas` | — | Opens map |
| `biblestudio://atlas/location/{id}` | `biblestudio://atlas/location/42` | Location detail |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes on verse ref tap |
