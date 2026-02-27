# Highlights — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `HighlightsConfig.Main` | — | Highlight manager |
| `HighlightsConfig.ForVerse(verseId)` | `globalVerseId: Int` | Highlights for a verse |

```kotlin
sealed class HighlightsConfig : Parcelable {
    @Parcelize data object Main : HighlightsConfig()
    @Parcelize data class ForVerse(val globalVerseId: Int) : HighlightsConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to highlighted verse |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("highlights")
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://highlights` | — | Opens highlight manager |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes on highlight tap |

---

## 6. Inter-Module Communication Diagram

```
+---------------+  VerseSelected     +------------+
| Bible Reader  | ----------------> | Highlights  |
+---------------+                   +------+------+
       ^                                   |
       | VerseSelected (on tap)            |
       +-----------------------------------+
```
