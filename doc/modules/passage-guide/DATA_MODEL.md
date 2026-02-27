# Passage Guide — Data Model

> Domain entities and consumed repositories. Passage Guide does not own database tables.

---

## 1. Domain Entities

### 1.1 PassageReport

```kotlin
data class PassageReport(
    val verseId: Int,
    val verseText: String,
    val crossReferences: List<CrossReference>,
    val keyWords: List<LexiconEntry>,
    val commentaryEntries: List<ResourceEntry>,
    val userNotes: List<Note>,
    val morphologyWords: List<MorphWord>,
)
```

| Field | Type | Description | Source Module |
|-------|------|-------------|--------------|
| `verseId` | `Int` | `BBCCCVVV` verse reference | bible-reader |
| `verseText` | `String` | Full verse text | bible-reader |
| `crossReferences` | `List<CrossReference>` | Related verses | cross-references |
| `keyWords` | `List<LexiconEntry>` | Important words with definitions | word-study |
| `commentaryEntries` | `List<ResourceEntry>` | Commentary entries for verse | resource-library |
| `userNotes` | `List<Note>` | User's notes on this verse | note-editor |
| `morphologyWords` | `List<MorphWord>` | Word-level linguistic data | morphology-interlinear |

---

## 2. SQLite Schema

**Passage Guide owns no tables.** All data is read from other module repositories:

| Repository consumed | Module | Method called |
|--------------------|--------|---------------|
| `BibleRepository` | bible-reader | `getVerseById(verseId)` |
| `CrossRefRepository` | cross-references | `getReferences(verseId)` |
| `WordStudyRepository` | word-study | `getEntry(strongsNumber)` per key word |
| `ResourceRepository` | resource-library | `getEntriesForVerse(verseId)` |
| `NoteRepository` | note-editor | `getNotesForVerse(verseId)` |
| `MorphologyRepository` | morphology-interlinear | `getMorphology(verseId)` |

---

## 3. Key Queries (Delegated)

Since Passage Guide delegates to other repositories, all queries are executed in parallel via `async {}`:

```kotlin
suspend fun buildReport(verseId: Int): PassageReport = coroutineScope {
    val verseDeferred = async { bibleRepository.getVerseById(verseId) }
    val crossRefsDeferred = async { crossRefRepository.getReferences(verseId) }
    val commentaryDeferred = async { resourceRepository.getEntriesForVerse(verseId) }
    val notesDeferred = async { noteRepository.getNotesForVerse(verseId) }
    val morphDeferred = async { morphologyRepository.getMorphology(verseId) }

    PassageReport(
        verseId = verseId,
        verseText = verseDeferred.await().getOrDefault(""),
        crossReferences = crossRefsDeferred.await().getOrDefault(emptyList()),
        commentaryEntries = commentaryDeferred.await().getOrDefault(emptyList()),
        userNotes = notesDeferred.await().getOrDefault(emptyList()),
        morphologyWords = morphDeferred.await().getOrDefault(emptyList()),
        keyWords = emptyList(), // extracted from morphology post-processing
    )
}
```

---

## 4. Relations with Other Modules

| External Repository | Data consumed | Type |
|--------------------|---------------|------|
| `BibleRepository` | Verse text | Read-only |
| `CrossRefRepository` | References | Read-only |
| `WordStudyRepository` | Lexicon entries | Read-only |
| `ResourceRepository` | Commentary entries | Read-only |
| `NoteRepository` | User notes | Read-only |
| `MorphologyRepository` | Morphology words | Read-only |
