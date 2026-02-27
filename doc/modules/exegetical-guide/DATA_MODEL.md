# Exegetical Guide — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 ExegeticalData (aggregated)

```kotlin
data class ExegeticalData(
    val globalVerseId: Int,
    val verseText: String,
    val morphology: List<MorphologyWord>,
    val wordStudies: List<WordStudyEntry>,
    val crossReferences: List<CrossReference>,
    val commentaries: List<CommentaryEntry>,
    val contextVerses: List<Verse>,
)
```

### 1.2 Component Types (from other modules)

| Type | Source Module | Description |
|------|-------------|-------------|
| `MorphologyWord` | morphology-interlinear | Parsed word with lemma/morph code |
| `WordStudyEntry` | word-study | Lexicon entry with definitions |
| `CrossReference` | cross-references | Related verse with type |
| `CommentaryEntry` | resource-library | Commentary text for verse |
| `Verse` | bible-reader | Verse text for context |

---

## 2. SQLite Schema

**No dedicated tables.** Exegetical Guide is a pure aggregation module.

---

## 3. Repositories

No dedicated repository. Directly depends on:

| Repository | Module | Usage |
|-----------|--------|-------|
| `MorphologyRepository` | morphology-interlinear | Word parsing |
| `WordStudyRepository` | word-study | Definitions |
| `CrossRefRepository` | cross-references | Related passages |
| `ResourceRepository` | resource-library | Commentaries |
| `BibleRepository` | bible-reader | Context verses |

---

## 4. Key Queries

All queries are from other modules. See their DATA_MODEL docs.

| Query | Source | Parameters | Performance |
|-------|--------|-----------|-------------|
| `morphologyForVerse` | Morphology.sq | `globalVerseId` | < 10 ms |
| `entryByStrongs` | Lexicon.sq | `strongsNumber` | < 5 ms |
| `refsForVerse` | CrossRef.sq | `globalVerseId` | < 5 ms |
| `entriesForVerse` | Resource.sq | `globalVerseId` | < 20 ms |
| `versesInRange` | Bible.sq | `startId, endId` | < 5 ms |

---

## 5. Migrations

N/A — No own tables.

---

## 6. Relations with Other Modules

| Module | Relation | Direction |
|--------|----------|-----------|
| morphology-interlinear | Reads morphology data | Depends on |
| word-study | Reads lexicon entries | Depends on |
| cross-references | Reads cross-ref data | Depends on |
| resource-library | Reads commentary entries | Depends on |
| bible-reader | Reads verse text | Depends on |
