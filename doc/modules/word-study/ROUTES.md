# Word Study — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `WordStudyConfig.Main` | — | Open empty; waits for VerseBus |
| `WordStudyConfig.Detail(strongsNumber)` | `strongsNumber: String` | Open with specific Strong's entry |

```kotlin
sealed class WordStudyConfig : Parcelable {
    @Parcelize data object Main : WordStudyConfig()
    @Parcelize data class Detail(val strongsNumber: String) : WordStudyConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `bible-reader` | via VerseBus `VerseSelected` | `globalVerseId` | When user taps a verse occurrence |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("word_study", config = mapOf("strongs" to "H1254"))
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://wordstudy/{strongsNumber}` | `biblestudio://wordstudy/H1254` | Opens word study for H1254 |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `StrongsSelected`; publishes `VerseSelected` on occurrence tap |

---

## 6. Inter-Module Communication Diagram

```
┌──────────────┐  StrongsSelected   ┌──────────────┐
│ Morphology   │ ──────────────────→ │  Word Study  │
└──────────────┘                     └──────┬───────┘
┌──────────────┐  StrongsSelected           │
│ Bible Reader │ ──────────────────→         │
└──────────────┘                             │
       ▲         VerseSelected               │
       └─────────────────────────────────────┘
```
