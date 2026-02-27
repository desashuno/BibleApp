# Bible Reader — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.
> Modules communicate exclusively through Verse Bus events, Decompose child navigation, and config-based deep links.

---

## 1. Exposed Configurations

### 1.1 Configuration Table

| Config | Parameters | Description |
|--------|-----------|-------------|
| `BibleReaderConfig.Main` | — | Default chapter view (loads last read or Gen 1) |
| `BibleReaderConfig.Chapter(bookId, chapter)` | `bookId: Int, chapter: Int` | Navigate to specific chapter |
| `BibleReaderConfig.Verse(globalVerseId)` | `globalVerseId: Int` | Navigate to specific verse and highlight it |
| `BibleReaderConfig.Comparison` | — | Open in text comparison mode |

### 1.2 Config Definition

```kotlin
sealed class BibleReaderConfig : Parcelable {
    @Parcelize data object Main : BibleReaderConfig()
    @Parcelize data class Chapter(val bookId: Int, val chapter: Int) : BibleReaderConfig()
    @Parcelize data class Verse(val globalVerseId: Int) : BibleReaderConfig()
    @Parcelize data object Comparison : BibleReaderConfig()
}
```

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `word-study` | via VerseBus `StrongsSelected` | `strongsNumber: String` | When user taps a linked Strong's number in HTML text |
| `cross-references` | via VerseBus `VerseSelected` | `globalVerseId: Int` | Cross-ref pane auto-updates on verse selection |
| `note-editor` | via VerseBus `VerseSelected` | `globalVerseId: Int` | Note pane loads notes for selected verse |

### 2.1 Navigation Examples

```kotlin
// User taps a Strong's number link in verse HTML
verseBus.publish(LinkEvent.StrongsSelected(strongsNumber = "H1254"))

// User taps a verse → all subscribing panes update
verseBus.publish(LinkEvent.VerseSelected(globalVerseId = 01001001))

// User selects a passage range
verseBus.publish(LinkEvent.PassageSelected(startVerseId = 01001001, endVerseId = 01001031))
```

---

## 3. Pane Opening (Workspace)

### 3.1 PaneRegistry Key

```kotlin
PaneRegistry.build("bible_reader", config = mapOf("bookId" to "1", "chapter" to "1"))
```

### 3.2 Pane Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `bookId` | `Int` | No | Target book (defaults to last read or Genesis) |
| `chapter` | `Int` | No | Target chapter (defaults to 1) |
| `globalVerseId` | `Int` | No | Scroll to specific verse after loading |

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://read/{bookId}/{chapter}` | `biblestudio://read/1/1` | Opens Genesis chapter 1 |
| `biblestudio://verse/{globalVerseId}` | `biblestudio://verse/01001001` | Opens and scrolls to Genesis 1:1 |
| `biblestudio://compare/{globalVerseId}` | `biblestudio://compare/01001001` | Opens comparison mode for Gen 1:1 |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both (Publisher + Subscriber)** | Publishes `VerseSelected` and `PassageSelected` on user interaction; subscribes to same events from other panes to scroll/navigate |

### 5.1 Publishing

```kotlin
// When the user taps a verse
verseBus.publish(LinkEvent.VerseSelected(globalVerseId = verse.globalVerseId))

// When the user long-presses and selects a range
verseBus.publish(LinkEvent.PassageSelected(
    startVerseId = selectionStart.globalVerseId,
    endVerseId = selectionEnd.globalVerseId,
))

// When the user taps a Strong's number link
verseBus.publish(LinkEvent.StrongsSelected(strongsNumber = "H1254"))
```

### 5.2 Subscribing

```kotlin
scope.launch {
    verseBus.events.collect { event ->
        when (event) {
            is LinkEvent.VerseSelected -> scrollToVerse(event.globalVerseId)
            is LinkEvent.PassageSelected -> loadPassageRange(event.startVerseId, event.endVerseId)
            is LinkEvent.SearchResult -> navigateToVerse(event.globalVerseId)
            else -> { /* ignore StrongsSelected, ResourceSelected */ }
        }
    }
}
```

---

## 6. Inter-Module Communication Diagram

```
                    ┌──────────────┐
              ┌────→│ Cross-Refs   │  (subscribes VerseSelected)
              │     └──────────────┘
              │     ┌──────────────┐
              ├────→│ Word Study   │  (subscribes StrongsSelected)
              │     └──────────────┘
              │     ┌──────────────┐
              ├────→│ Morphology   │  (subscribes VerseSelected)
 VerseSelected│     └──────────────┘
 StrongsSelected    ┌──────────────┐
 PassageSelected├──→│ Passage Guide│  (subscribes VerseSelected)
              │     └──────────────┘
┌─────────────┤     ┌──────────────┐
│ Bible Reader ├───→│ Note Editor  │  (subscribes VerseSelected)
└──────┬──────┘     └──────────────┘
       │            ┌──────────────┐
       ├───────────→│ Highlights   │  (subscribes VerseSelected)
       │            └──────────────┘
       │            ┌──────────────┐
       │←───────────│ Search       │  (publishes SearchResult → reader scrolls)
       │            └──────────────┘
       │            ┌──────────────┐
       │←──────────→│ Audio Sync   │  (bidirectional VerseSelected)
                    └──────────────┘
```
