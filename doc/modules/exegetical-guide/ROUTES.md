# Exegetical Guide — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `ExegeticalGuideConfig.Main` | — | Awaiting verse selection |
| `ExegeticalGuideConfig.ForVerse(verseId)` | `globalVerseId: Int` | Pre-loaded for verse |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to cross-ref verse |
| `word-study` | via VerseBus | Open Strong's number study |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://exegetical-guide` | — | Opens empty guide |
| `biblestudio://exegetical-guide/{verseId}` | `biblestudio://exegetical-guide/43003016` | Guide for John 3:16 |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes `VerseSelected` and `StrongsSelected` |
