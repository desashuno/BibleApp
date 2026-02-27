# Cross-References — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `CrossReferenceConfig.Main` | — | Open empty; waits for VerseBus event |
| `CrossReferenceConfig.ForVerse(globalVerseId)` | `globalVerseId: Int` | Open with pre-loaded references |

```kotlin
sealed class CrossReferenceConfig : Parcelable {
    @Parcelize data object Main : CrossReferenceConfig()
    @Parcelize data class ForVerse(val globalVerseId: Int) : CrossReferenceConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `bible-reader` | via VerseBus `VerseSelected` | `targetVerseId: Int` | When user taps a cross-reference |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("cross_references", config = mapOf("verseId" to "01001001"))
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://crossrefs/{verseId}` | `biblestudio://crossrefs/01001001` | Opens cross-refs for Gen 1:1 |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected` to load refs; publishes `VerseSelected` on reference tap |

---

## 6. Inter-Module Communication Diagram

```
┌──────────────┐  VerseSelected     ┌──────────────────┐
│ Bible Reader │ ──────────────────→ │ Cross-References │
└──────────────┘                     └────────┬─────────┘
       ▲                                      │
       │          VerseSelected (target)       │
       └──────────────────────────────────────┘
```
