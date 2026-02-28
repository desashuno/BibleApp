# Open Data Sources

> BibleStudio — Mapping of open-source data to app modules

---

## Overview

All data in BibleStudio comes from **open-source, public-domain, or freely licensed** projects. Raw data is downloaded into `data-pipeline/raw/` and then normalized into SQLite databases that match the app's schema. No proprietary or copyrighted data is used.

### Pipeline Flow

```
Open Sources (GitHub, OpenBible.info, etc.)
  → data-pipeline/raw/          (original files: TSV, JSON, XML, OSIS, CSV)
    → data-pipeline/normalize.py  (Python scripts)
      → data-pipeline/output/     (SQLite databases ready for the app)
```

---

## 1. Bible Text

### Module: `bible-reader`
**Tables**: `bibles`, `books`, `chapters`, `verses`, `fts_verses`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **scrollmapper/bible_databases** | MIT | SQLite, CSV, JSON | https://github.com/scrollmapper/bible_databases |
| **seven1m/open-bibles** | Public Domain / CC BY | OSIS XML, Zefania XML | https://github.com/seven1m/open-bibles |
| **Beblia/Holy-Bible-XML-Format** | Public Domain (per-file `status` attribute) | XML | https://github.com/Beblia/Holy-Bible-XML-Format |

**Available translations (public domain / free license)**:

*English:*
- **KJV** — King James Version (1769), public domain
- **ASV** — American Standard Version (1901), public domain
- **WEB** — World English Bible, public domain
- **YLT** — Young's Literal Translation, public domain
- **DRB** — Douay-Rheims Bible, public domain
- **BBE** — Bible in Basic English, public domain

*Spanish (via Beblia):*
- **SE1569** — Sagradas Escrituras 1569, public domain
- **RV1909** — Reina-Valera 1909, dominio público
- **RVES** — Reina-Valera Española, public domain
- **VBL** — Versión Biblia Libre 2022, CC BY-SA 4.0

*Greek:*
- **SBLGNT** — SBL Greek New Testament, SBLGNT License (free)

**Raw download location**: `data-pipeline/raw/bibles/`

---

## 2. Hebrew & Greek Original Texts

### Module: `morphology-interlinear`
**Tables**: `morphology`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **STEPBible TAHOT** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data/tree/master/Translators%20Amalgamated%20OT%2BNT |
| **STEPBible TAGNT** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data/tree/master/Translators%20Amalgamated%20OT%2BNT |
| **OpenScriptures morphhb** | CC BY 4.0 | OSIS XML | https://github.com/openscriptures/morphhb |
| **MorphGNT / SBLGNT** | CC BY-SA 3.0 | CSV | https://github.com/morphgnt/sblgnt |

**Data provided**:
- Hebrew word-by-word morphology (Leningrad Codex)
- Greek word-by-word morphology (NA27/28, TR, SBLGNT)
- Strong's numbers per word
- Morphology parsing codes (verb, noun, tense, voice, mood, etc.)
- Surface forms, lemmas
- Context-sensitive glosses

**Raw download location**: `data-pipeline/raw/morphology/`

---

## 3. Lexicon / Word Study

### Module: `word-study`
**Tables**: `lexicon_entries`, `word_occurrences`, `fts_lexicon`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **STEPBible TBESH** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |
| **STEPBible TBESG** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |
| **STEPBible TFLSJ** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |
| **OpenScriptures Hebrew Lexicon** | CC BY 4.0 | XML | https://github.com/openscriptures/HebrewLexicon |
| **Abbott-Smith Greek Lexicon** | Public Domain | TEI XML | https://github.com/translatable-exegetical-tools/Abbott-Smith |

**Data provided**:
- **TBESH**: Abridged BDB Hebrew lexicon with extended Strong's numbers
- **TBESG**: Brief Greek lexicon for NT, LXX, Apocrypha
- **TFLSJ**: Full LSJ Greek lexicon entries for all Bible words
- Hebrew definitions, transliterations, usage notes
- Greek definitions, transliterations, usage notes

**Raw download location**: `data-pipeline/raw/lexicon/`

---

## 4. Cross-References

### Module: `cross-references`
**Tables**: `cross_references`, `parallel_passages`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **OpenBible.info Cross-References** | CC BY 4.0 | CSV | https://www.openbible.info/labs/cross-references/ |
| **scrollmapper/bible_databases** | MIT | SQLite, CSV | https://github.com/scrollmapper/bible_databases |
| **Treasury of Scripture Knowledge** | Public Domain | Various | (included in OpenBible.info data) |

**Data provided**:
- ~340,000 cross-reference pairs (verse → verse)
- Vote-weighted confidence scores
- Source primarily from Treasury of Scripture Knowledge (public domain)
- Gospel parallel passages (synoptic data)

**Raw download location**: `data-pipeline/raw/cross-references/`

---

## 5. Geographic Data (Atlas)

### Module: `theological-atlas`
**Tables**: `geographic_locations`, `location_verse_index`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **openbibleinfo/Bible-Geocoding-Data** | CC BY 4.0 | JSONL | https://github.com/openbibleinfo/Bible-Geocoding-Data |
| **STEPBible TIPNR (places)** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |

**Data provided**:
- ~1,000 biblical places with latitude/longitude
- Modern name associations
- Verse references per location
- Confidence/precision scores for coordinates
- GeoJSON geometries for regions

**Raw download location**: `data-pipeline/raw/geography/`

---

## 6. People, Entities & Knowledge Graph

### Module: `knowledge-graph`
**Tables**: `entities`, `relationships`, `entity_verse_index`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **STEPBible TIPNR** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |
| **Viz.Bible people/places/events** | Free (upon request) | CSV, JSON | https://viz.bible/bible-data/ |

**Data provided**:
- Every proper noun in the Bible, disambiguated into individual persons/places/things
- Family relationships (parents, siblings, spouses, offspring)
- Aliases and alternate name forms
- Exhaustive verse references per individual
- Brief, short, and article-length descriptions

**Raw download location**: `data-pipeline/raw/entities/`

---

## 7. Timeline Events

### Module: `timeline`
**Tables**: `timeline_events`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **Viz.Bible events** | Free (upon request) | CSV, JSON | https://viz.bible/bible-data/ |
| **STEPBible TIPNR (events)** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |

**Data provided**:
- Biblical events with approximate dates (BCE/CE)
- Duration and period groupings
- Linked participants and locations
- Verse references per event

**Raw download location**: `data-pipeline/raw/timeline/`

---

## 8. Reading Plans

### Module: `reading-plans`
**Tables**: `reading_plans`, `reading_plan_progress`

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **Custom-generated** | Project-owned | JSON | Generated from verse counts |

**Data provided** (generated, not downloaded):
- Chronological reading plan
- Book-by-book reading plan
- Old Testament + New Testament parallel plan
- 90-day whole Bible plan
- Gospels plan, Psalms & Proverbs plan

**Raw download location**: `data-pipeline/raw/reading-plans/`

---

## 9. Versification

### Module: shared (all modules)

| Source | License | Format | URL |
|--------|---------|--------|-----|
| **STEPBible TVTMS** | CC BY 4.0 | TSV | https://github.com/STEPBible/STEPBible-Data |

**Data provided**:
- Versification differences between English, Hebrew, Latin, Greek traditions
- Mapping rules for normalizing to BBCCCVVV global verse IDs

**Raw download location**: `data-pipeline/raw/versification/`

---

## License Summary

| Source | License | Commercial Use | Attribution Required |
|--------|---------|---------------|---------------------|
| scrollmapper/bible_databases | MIT | ✅ | ✅ |
| seven1m/open-bibles | Public Domain / CC BY | ✅ | Varies |
| STEPBible-Data | CC BY 4.0 | ✅ | ✅ (Tyndale House, Cambridge) |
| OpenScriptures morphhb | CC BY 4.0 | ✅ | ✅ |
| MorphGNT/SBLGNT | CC BY-SA 3.0 | ✅ | ✅ + ShareAlike |
| OpenBible.info Geo | CC BY 4.0 | ✅ | ✅ |
| OpenBible.info Cross-Refs | CC BY 4.0 | ✅ | ✅ |
| Abbott-Smith Lexicon | Public Domain | ✅ | ❌ |
| KJV / ASV / WEB / YLT | Public Domain | ✅ | ❌ |
| Beblia/Holy-Bible-XML-Format | Public Domain (per-file) | ✅ | ❌ |
| Viz.Bible | Free upon request | ✅ | ✅ |

---

## Pipeline Output Statistics

Expected minimum row counts after a full pipeline run (`python normalize.py --verify`):

| Table                  | Minimum Rows |
|------------------------|-------------:|
| `bibles`               |            3 |
| `books`                |           60 |
| `verses`               |      150,000 |
| `lexicon_entries`      |       10,000 |
| `morphology`           |      100,000 |
| `word_occurrences`     |      200,000 |
| `cross_references`     |      300,000 |
| `geographic_locations` |        1,000 |
| `entities`             |       10,000 |
| `relationships`        |        3,000 |
| `timeline_events`      |           50 |
| `reading_plans`        |            4 |

FTS5 virtual tables: `fts_verses`, `fts_lexicon`, `fts_notes`, `fts_resources`, `fts_entities`.

Seed database size: ~80–120 MB (varies with data source updates).

---

## Attribution Notice

BibleStudio must include the following attributions in Settings → About:

```
Bible text data:
- King James Version (1769) — Public Domain
- American Standard Version (1901) — Public Domain
- World English Bible — Public Domain
- Young's Literal Translation — Public Domain
- Bible in Basic English — Public Domain
- Sagradas Escrituras 1569 — Public Domain (via Beblia)
- Reina-Valera 1909 — Dominio Público (via Beblia)
- Reina-Valera Española — Public Domain (via Beblia)
- Versión Biblia Libre 2022 — CC BY-SA 4.0 (via Beblia)

Morphology & Lexicon:
- STEPBible Data © Tyndale House, Cambridge — CC BY 4.0
- Open Scriptures Hebrew Bible — CC BY 4.0
- MorphGNT/SBLGNT — CC BY-SA 3.0
- Abbott-Smith Greek Lexicon — Public Domain

Cross-References:
- OpenBible.info Cross-References — CC BY 4.0
- Treasury of Scripture Knowledge — Public Domain

Geographic Data:
- OpenBible.info Bible Geocoding — CC BY 4.0

Bible Databases:
- scrollmapper/bible_databases — MIT License
```
