# Audio Sync — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `AudioSyncConfig.Main` | — | Audio player (no audio loaded) |
| `AudioSyncConfig.ForVerse(bibleId, verseId)` | `bibleId: Long, globalVerseId: Int` | Play from specific verse |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Highlight current verse during playback |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://audio` | — | Opens audio player |
| `biblestudio://audio/{bibleId}/{verseId}` | `biblestudio://audio/1/01001001` | Play Gen 1:1 |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected` (seek); publishes as audio advances |
