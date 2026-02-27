# Morphology / Interlinear — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `InterlinearConfig.Main` | — | Open empty; waits for VerseBus |
| `InterlinearConfig.ForVerse(globalVerseId)` | `globalVerseId: Int` | Open with pre-loaded verse |

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `word-study` | via VerseBus `StrongsSelected` | `strongsNumber: String` | When user taps an interlinear word |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("morphology", config = mapOf("verseId" to "01001001"))
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://interlinear/{verseId}` | `biblestudio://interlinear/01001001` | Opens interlinear for Gen 1:1 |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes `StrongsSelected` on word tap |

---

## 6. Inter-Module Communication Diagram

```
┌──────────────┐  VerseSelected     ┌──────────────┐  StrongsSelected  ┌──────────────┐
│ Bible Reader │ ──────────────────→ │ Morphology   │ ────────────────→ │ Word Study   │
└──────────────┘                     └──────────────┘                   └──────────────┘
```
