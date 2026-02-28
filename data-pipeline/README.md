# Data Pipeline

> Downloads open-source Bible data and normalizes it into SQLite for BibleStudio.

---

## Quick Start

```bash
# 1. Install Python dependencies
pip install -r requirements.txt

# 2. Download all raw data from open sources
python download.py

# 3. Normalize raw data into SQLite databases
python normalize.py

# 4. (Optional) Download + normalize in one step
python download.py && python normalize.py
```

## Folder Structure

```
data-pipeline/
├── README.md                    # This file
├── requirements.txt             # Python dependencies
├── download.py                  # Downloads raw data from open sources
├── normalize.py                 # Orchestrates all normalizers
├── normalizers/                 # Individual normalizer modules
│   ├── __init__.py
│   ├── bible_text.py            # bibles, books, chapters, verses
│   ├── morphology.py            # morphology (Hebrew + Greek)
│   ├── lexicon.py               # lexicon_entries, word_occurrences
│   ├── cross_references.py      # cross_references, parallel_passages
│   ├── geography.py             # geographic_locations
│   ├── entities.py              # entities, relationships (knowledge graph)
│   ├── timeline.py              # timeline_events
│   └── reading_plans.py         # reading_plans (generated)
├── raw/                         # Downloaded raw files (gitignored)
│   ├── bibles/
│   ├── morphology/
│   ├── lexicon/
│   ├── cross-references/
│   ├── geography/
│   ├── entities/
│   ├── timeline/
│   ├── versification/
│   └── reading-plans/
└── output/                      # Generated SQLite databases (gitignored)
    └── biblestudio-seed.db
```

## How It Works

### 1. Download (`download.py`)

Downloads raw data from open-source repositories into `raw/`. Sources include:

| Data | Source | Format |
|------|--------|--------|
| Bible translations | scrollmapper/bible_databases | SQLite |
| Bible translations (XML) | seven1m/open-bibles | OSIS XML |
| Hebrew morphology | STEPBible TAHOT | TSV |
| Greek morphology | STEPBible TAGNT | TSV |
| Hebrew lexicon | STEPBible TBESH | TSV |
| Greek lexicon | STEPBible TBESG | TSV |
| Cross-references | OpenBible.info | CSV |
| Geography | openbibleinfo/Bible-Geocoding-Data | JSONL |
| People & Places | STEPBible TIPNR | TSV |

### 2. Normalize (`normalize.py`)

Reads raw files and writes normalized data into a single SQLite database (`output/biblestudio-seed.db`) that matches the app's schema. Each normalizer module handles one data domain.

### 3. Output

The output database can be:
- Bundled with the app as a pre-populated seed database
- Imported at first launch via the module system
- Used as a reference for development and testing

## Data Sources

See [doc/OPEN_DATA_SOURCES.md](../doc/OPEN_DATA_SOURCES.md) for the complete list of open-source data, licenses, and attribution requirements.

## Adding a New Data Source

1. Add the download URL to `download.py` in the appropriate section
2. Create or update a normalizer in `normalizers/`
3. Register the normalizer in `normalize.py`
4. Update `doc/OPEN_DATA_SOURCES.md` with the new source
5. Run `python download.py && python normalize.py` to verify

## Verification

After building the seed database, verify it meets minimum row-count thresholds:

```bash
python normalize.py --verify
```

This checks all key tables against expected minimums (e.g., verses ≥ 150K,
cross_references ≥ 300K) and exits with code 1 on failure.

You can also verify a custom output file:

```bash
python normalize.py --verify --output custom-seed.db
```

## CI / GitHub Actions

The pipeline runs automatically on pushes to `data-pipeline/` via
`.github/workflows/data-pipeline.yml`:

1. Installs Python 3.12 + `requirements.txt`
2. Downloads raw data (cached between runs)
3. Runs `python normalize.py` to build the seed DB
4. Runs `python normalize.py --verify` to validate
5. Uploads `biblestudio-seed.db` as a build artifact (30-day retention)

## Seed Database Bundling

The output `biblestudio-seed.db` is bundled into each platform's app assets:

| Platform | Location |
|----------|----------|
| Desktop  | `shared/src/desktopMain/resources/biblestudio-seed.db` (classpath) |
| Android  | `composeApp/src/androidMain/assets/biblestudio-seed.db` |
| iOS      | Xcode bundle resource via Gradle `iosResources` |

On first launch, `copySeedDatabaseIfNeeded()` copies the bundled seed DB to the
app's data directory. Subsequent launches skip the copy if the file already exists.

## Notes

- `raw/` and `output/` are gitignored — they contain large binary/data files
- The pipeline is idempotent: running it again overwrites previous output
- All sources are verified open-source or public domain (see OPEN_DATA_SOURCES.md)
- Python 3.10+ required
