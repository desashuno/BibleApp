#!/usr/bin/env python3
"""
download.py — Downloads open-source Bible data into raw/ subfolders.

Sources:
  - scrollmapper/bible_databases          (Bible text, cross-references)
  - Beblia/Holy-Bible-XML-Format          (Bible text in XML — Spanish & more)
  - STEPBible-Data                        (morphology, lexicon, entities)
  - openbibleinfo                         (geography, cross-references)
  - openscriptures/morphhb               (Hebrew morphology)
  - morphgnt/sblgnt                       (Greek morphology)
  - Public-domain commentaries            (Matthew Henry, John Gill, JFB)

Usage:
    python download.py              # download everything
    python download.py --only bibles morphology lexicon
    python download.py --only beblia-bibles commentaries
"""

from __future__ import annotations

import argparse
import io
import os
import sys
import zipfile
from pathlib import Path

import requests
from tqdm import tqdm

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

PIPELINE_DIR = Path(__file__).resolve().parent
RAW_DIR = PIPELINE_DIR / "raw"

SUBDIRS = [
    "bibles",
    "morphology",
    "lexicon",
    "cross-references",
    "geography",
    "entities",
    "timeline",
    "versification",
    "reading-plans",
    "dictionaries",
    "commentaries",
]

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def ensure_dirs() -> None:
    """Create raw/ subdirectories if they don't exist."""
    for sub in SUBDIRS:
        (RAW_DIR / sub).mkdir(parents=True, exist_ok=True)


def download_file(url: str, dest: Path, description: str | None = None) -> None:
    """Download a single file with a progress bar."""
    if dest.exists():
        print(f"  ✓ Already exists: {dest.name}")
        return

    label = description or dest.name
    print(f"  ↓ Downloading: {label}")

    resp = requests.get(url, stream=True, timeout=120)
    resp.raise_for_status()

    total = int(resp.headers.get("content-length", 0))
    with open(dest, "wb") as f, tqdm(
        total=total, unit="B", unit_scale=True, desc=f"    {dest.name}", leave=False
    ) as bar:
        for chunk in resp.iter_content(chunk_size=8192):
            f.write(chunk)
            bar.update(len(chunk))


def download_github_raw(repo: str, branch: str, filepath: str, dest: Path, desc: str | None = None) -> None:
    """Download a single file from a GitHub repository."""
    url = f"https://raw.githubusercontent.com/{repo}/{branch}/{filepath}"
    download_file(url, dest, desc)


def download_github_archive(repo: str, branch: str, dest_dir: Path, desc: str | None = None) -> None:
    """Download and extract a GitHub repo archive (ZIP)."""
    marker = dest_dir / ".downloaded"
    if marker.exists():
        print(f"  ✓ Already downloaded: {repo}")
        return

    label = desc or repo
    print(f"  ↓ Downloading archive: {label}")

    url = f"https://github.com/{repo}/archive/refs/heads/{branch}.zip"
    resp = requests.get(url, stream=True, timeout=300)
    resp.raise_for_status()

    total = int(resp.headers.get("content-length", 0))
    buf = io.BytesIO()
    with tqdm(total=total, unit="B", unit_scale=True, desc=f"    {repo}", leave=False) as bar:
        for chunk in resp.iter_content(chunk_size=8192):
            buf.write(chunk)
            bar.update(len(chunk))

    print(f"    Extracting...")
    buf.seek(0)
    with zipfile.ZipFile(buf) as zf:
        zf.extractall(dest_dir)

    marker.touch()


# ---------------------------------------------------------------------------
# Download functions per data domain
# ---------------------------------------------------------------------------

def download_bibles() -> None:
    """Download Bible text databases from scrollmapper/bible_databases."""
    print("\n📖 Bible Text")

    dest_dir = RAW_DIR / "bibles"

    # scrollmapper bible_databases — SQLite format (2024 branch has stable schema)
    # The main SQLite database (includes all translations + cross-references)
    download_github_raw(
        "scrollmapper/bible_databases", "2024",
        "bible-sqlite.db",
        dest_dir / "scrollmapper-bible.db",
        "scrollmapper SQLite (multi-translation)"
    )

    # Cross-references source text from scrollmapper (optional — redundant with OpenBible.info)
    try:
        download_github_raw(
            "scrollmapper/bible_databases", "2024",
            "cross_references.txt",
            dest_dir / "cross_references_scrollmapper.txt",
            "scrollmapper cross-references source"
        )
    except Exception:
        print("    ⚠ scrollmapper cross_references.txt skipped (non-critical)")


def _download_and_concat(repo: str, branch: str, file_parts: list[str], dest: Path, desc: str) -> None:
    """Download multiple files from a GitHub repo and concatenate them into one."""
    if dest.exists():
        print(f"  ✓ Already exists: {dest.name}")
        return

    print(f"  ↓ Downloading & merging: {desc} ({len(file_parts)} parts)")
    with open(dest, "wb") as out:
        for part_path in file_parts:
            url = f"https://raw.githubusercontent.com/{repo}/{branch}/{part_path}"
            resp = requests.get(url, stream=True, timeout=300)
            resp.raise_for_status()
            for chunk in resp.iter_content(chunk_size=8192):
                out.write(chunk)
            out.write(b"\n")  # newline separator between parts
    print(f"    ✓ Merged {len(file_parts)} parts → {dest.name}")


def download_morphology() -> None:
    """Download Hebrew & Greek morphology data from STEPBible."""
    print("\n🔤 Morphology (Hebrew & Greek)")

    dest_dir = RAW_DIR / "morphology"
    step_repo = "STEPBible/STEPBible-Data"
    step_prefix = "Translators Amalgamated OT+NT"

    # STEPBible TAHOT — Hebrew OT (split into 4 files by book range)
    _download_and_concat(
        step_repo, "master",
        [
            f"{step_prefix}/TAHOT Gen-Deu - Translators Amalgamated Hebrew OT - STEPBible.org CC BY.txt",
            f"{step_prefix}/TAHOT Jos-Est - Translators Amalgamated Hebrew OT - STEPBible.org CC BY.txt",
            f"{step_prefix}/TAHOT Job-Sng - Translators Amalgamated Hebrew OT - STEPBible.org CC BY.txt",
            f"{step_prefix}/TAHOT Isa-Mal - Translators Amalgamated Hebrew OT - STEPBible.org CC BY.txt",
        ],
        dest_dir / "TAHOT.txt",
        "STEPBible TAHOT (Hebrew OT morphology)"
    )

    # STEPBible TAGNT — Greek NT (split into 2 files)
    _download_and_concat(
        step_repo, "master",
        [
            f"{step_prefix}/TAGNT Mat-Jhn - Translators Amalgamated Greek NT - STEPBible.org CC-BY.txt",
            f"{step_prefix}/TAGNT Act-Rev - Translators Amalgamated Greek NT - STEPBible.org CC-BY.txt",
        ],
        dest_dir / "TAGNT.txt",
        "STEPBible TAGNT (Greek NT morphology)"
    )

    # STEPBible Hebrew morphology codes
    download_github_raw(
        step_repo, "master",
        "TEHMC - Translators Expansion of Hebrew Morphology Codes - STEPBible.org CC BY.txt",
        dest_dir / "TEHMC.txt",
        "STEPBible TEHMC (Hebrew morphology codes)"
    )

    # STEPBible Greek morphology codes (note: repo has typo 'Morphhology')
    download_github_raw(
        step_repo, "master",
        "TEGMC - Translators Expansion of Greek Morphhology Codes - STEPBible.org CC BY.txt",
        dest_dir / "TEGMC.txt",
        "STEPBible TEGMC (Greek morphology codes)"
    )


def download_lexicon() -> None:
    """Download Hebrew & Greek lexicon data from STEPBible."""
    print("\n📚 Lexicon (Hebrew & Greek)")

    dest_dir = RAW_DIR / "lexicon"

    # TBESH — Hebrew lexicon (abridged BDB)
    download_github_raw(
        "STEPBible/STEPBible-Data", "master",
        "Lexicons/TBESH - Translators Brief lexicon of Extended Strongs for Hebrew - STEPBible.org CC BY.txt",
        dest_dir / "TBESH.txt",
        "STEPBible TBESH (Hebrew lexicon)"
    )

    # TBESG — Greek lexicon
    download_github_raw(
        "STEPBible/STEPBible-Data", "master",
        "Lexicons/TBESG - Translators Brief lexicon of Extended Strongs for Greek - STEPBible.org CC BY.txt",
        dest_dir / "TBESG.txt",
        "STEPBible TBESG (Greek lexicon)"
    )


def download_cross_references() -> None:
    """Download cross-reference data from OpenBible.info (zip archive)."""
    print("\n🔗 Cross-References")

    dest_dir = RAW_DIR / "cross-references"
    dest_file = dest_dir / "cross_references.txt"

    if dest_file.exists():
        print(f"  ✓ Already exists: {dest_file.name}")
        return

    # OpenBible.info cross-references (TSK-based) — distributed as a zip
    zip_url = "https://a.openbible.info/data/cross-references.zip"
    print(f"  ↓ Downloading: OpenBible.info cross-references (zip)")

    resp = requests.get(zip_url, stream=True, timeout=120)
    resp.raise_for_status()

    buf = io.BytesIO()
    for chunk in resp.iter_content(chunk_size=8192):
        buf.write(chunk)

    buf.seek(0)
    with zipfile.ZipFile(buf) as zf:
        # Find the .txt file inside the zip
        txt_names = [n for n in zf.namelist() if n.endswith(".txt")]
        if not txt_names:
            raise RuntimeError("No .txt file found in cross-references.zip")
        with zf.open(txt_names[0]) as src, open(dest_file, "wb") as dst:
            dst.write(src.read())

    print(f"    ✓ Extracted → {dest_file.name}")


def download_geography() -> None:
    """Download geographic data from openbibleinfo/Bible-Geocoding-Data."""
    print("\n🗺️  Geography")

    dest_dir = RAW_DIR / "geography"

    download_github_archive(
        "openbibleinfo/Bible-Geocoding-Data", "master",
        dest_dir,
        "OpenBible.info geocoding data"
    )


def download_entities() -> None:
    """Download people/places/things data from STEPBible TIPNR."""
    print("\n👤 Entities (People, Places, Things)")

    dest_dir = RAW_DIR / "entities"

    # TIPNR — Proper nouns with references
    download_github_raw(
        "STEPBible/STEPBible-Data", "master",
        "TIPNR - Translators Individualised Proper Names with all References - STEPBible.org CC BY.txt",
        dest_dir / "TIPNR.txt",
        "STEPBible TIPNR (proper nouns)"
    )


def download_versification() -> None:
    """Download versification mapping data from STEPBible."""
    print("\n🔢 Versification")

    dest_dir = RAW_DIR / "versification"

    download_github_raw(
        "STEPBible/STEPBible-Data", "master",
        "TVTMS - Translators Versification Traditions with Methodology for Standardisation for Eng+Heb+Lat+Grk+Others - STEPBible.org CC BY.txt",
        dest_dir / "TVTMS.txt",
        "STEPBible TVTMS (versification)"
    )


# ---------------------------------------------------------------------------
# Beblia/Holy-Bible-XML-Format — Public domain Bible XML files
# ---------------------------------------------------------------------------

# Only download translations with Public Domain or free license (CC BY-SA, etc.)
# Each entry: (filename_in_repo, local_filename, description)
BEBLIA_FREE_BIBLES = [
    ("Spanish1569Bible.xml", "beblia-Spanish1569.xml", "Sagradas Escrituras 1569 (Public Domain)"),
    ("SpanishBible.xml", "beblia-SpanishRV1909.xml", "Reina-Valera 1909 (Dominio Publico)"),
    ("SpanishRVESBible.xml", "beblia-SpanishRVES.xml", "Reina-Valera Espanola (Public Domain)"),
    ("SpanishVBL2022Bible.xml", "beblia-SpanishVBL2022.xml", "Version Biblia Libre 2022 (CC BY-SA 4.0)"),
]


def download_beblia_bibles() -> None:
    """Download public-domain Bible XML files from Beblia/Holy-Bible-XML-Format."""
    print("\n📖 Beblia Bible XML (Spanish — Public Domain)")

    dest_dir = RAW_DIR / "bibles"
    repo = "Beblia/Holy-Bible-XML-Format"
    branch = "master"

    for repo_filename, local_filename, description in BEBLIA_FREE_BIBLES:
        download_github_raw(
            repo, branch,
            repo_filename,
            dest_dir / local_filename,
            description,
        )


# ---------------------------------------------------------------------------
# Public-domain Bible Dictionaries
# ---------------------------------------------------------------------------

def download_dictionaries() -> None:
    """Download public-domain Bible dictionaries from GitHub."""
    print("\n📖 Bible Dictionaries (Public Domain)")

    dest_dir = RAW_DIR / "dictionaries"

    # Easton's Bible Dictionary (1893, public domain)
    # Source: historical-theology/Eastons-Bible-Dictionary on GitHub (JSON format)
    download_github_raw(
        "historical-theology/Eastons-Bible-Dictionary", "master",
        "easton.json",
        dest_dir / "easton.json",
        "Easton's Bible Dictionary (1893, public domain)",
    )

    # Smith's Bible Dictionary (public domain)
    # Source: LukeSmithxyz/KJV-bible-database — Smith's dictionary in JSON
    download_github_raw(
        "theBible0/SmithsBibleDictionary", "master",
        "smith.json",
        dest_dir / "smith.json",
        "Smith's Bible Dictionary (public domain)",
    )


# ---------------------------------------------------------------------------
# Public-domain Bible Commentaries
# ---------------------------------------------------------------------------

# Commentary sources from GitHub (public domain, various formats)
# Each entry: (repo, branch, files_in_repo, local_subdir, description)
COMMENTARY_SOURCES = [
    {
        "key": "matthew-henry",
        "repo": "scrollmapper/bible_databases_deuterocanonical",
        "branch": "master",
        "description": "Matthew Henry's Commentary (public domain)",
        "files": [
            # The scrollmapper repo includes commentary text alongside Bible data.
            # We download the full archive and extract commentary files.
        ],
        "archive": True,
    },
    {
        "key": "jfb",
        "repo": "scrollmapper/bible_databases_deuterocanonical",
        "branch": "master",
        "description": "Jamieson-Fausset-Brown Commentary (public domain)",
        "files": [],
        "archive": True,
    },
    {
        "key": "john-gill",
        "repo": "scrollmapper/bible_databases_deuterocanonical",
        "branch": "master",
        "description": "John Gill's Exposition (public domain)",
        "files": [],
        "archive": True,
    },
]


def download_commentaries() -> None:
    """
    Download public-domain Bible commentaries from GitHub.

    Sources include Matthew Henry's Commentary, John Gill's Exposition,
    and the Jamieson-Fausset-Brown Commentary. These are all public-domain
    works available in various formats across several GitHub repositories.
    """
    print("\n📝 Bible Commentaries (Public Domain)")

    dest_dir = RAW_DIR / "commentaries"
    dest_dir.mkdir(parents=True, exist_ok=True)

    # -----------------------------------------------------------------------
    # Matthew Henry's Commentary
    # Available from BibleHub-scraping repos and open-source Bible projects.
    # We try multiple known sources.
    # -----------------------------------------------------------------------
    mh_dir = dest_dir / "matthew-henry"
    mh_dir.mkdir(parents=True, exist_ok=True)

    # Source: ericpgreen/Matthew-Henry-Commentary on GitHub (TSV format)
    try:
        download_github_archive(
            "ericpgreen/Matthew-Henry-Commentary", "master",
            mh_dir,
            "Matthew Henry's Commentary (public domain)"
        )
    except Exception as e:
        print(f"    ⚠ Matthew Henry archive failed: {e}")
        # Fallback: try alternative repos
        try:
            download_github_archive(
                "jbeaulieu/matthew-henry-commentary", "master",
                mh_dir,
                "Matthew Henry's Commentary (fallback source)"
            )
        except Exception as e2:
            print(f"    ⚠ Matthew Henry fallback also failed: {e2}")
            print("    ⚠ You may need to manually place Matthew Henry data in raw/commentaries/matthew-henry/")

    # -----------------------------------------------------------------------
    # Jamieson-Fausset-Brown Commentary
    # -----------------------------------------------------------------------
    jfb_dir = dest_dir / "jfb"
    jfb_dir.mkdir(parents=True, exist_ok=True)

    try:
        download_github_archive(
            "ircdocs/jfb-commentary", "main",
            jfb_dir,
            "Jamieson-Fausset-Brown Commentary (public domain)"
        )
    except Exception:
        try:
            download_github_archive(
                "chadwalt/JFB-Commentary", "master",
                jfb_dir,
                "JFB Commentary (fallback source)"
            )
        except Exception as e2:
            print(f"    ⚠ JFB Commentary download failed: {e2}")
            print("    ⚠ You may need to manually place JFB data in raw/commentaries/jfb/")

    # -----------------------------------------------------------------------
    # John Gill's Exposition
    # -----------------------------------------------------------------------
    gill_dir = dest_dir / "john-gill"
    gill_dir.mkdir(parents=True, exist_ok=True)

    try:
        download_github_archive(
            "chadwalt/John-Gill-Exposition", "master",
            gill_dir,
            "John Gill's Exposition (public domain)"
        )
    except Exception:
        try:
            download_github_archive(
                "ircdocs/john-gill-exposition", "main",
                gill_dir,
                "John Gill's Exposition (fallback source)"
            )
        except Exception as e2:
            print(f"    ⚠ John Gill download failed: {e2}")
            print("    ⚠ You may need to manually place John Gill data in raw/commentaries/john-gill/")

    print("    Done (commentary downloads attempted)")


# ---------------------------------------------------------------------------
# Registry
# ---------------------------------------------------------------------------

DOWNLOAD_MAP = {
    "bibles": download_bibles,
    "beblia-bibles": download_beblia_bibles,
    "morphology": download_morphology,
    "lexicon": download_lexicon,
    "cross-references": download_cross_references,
    "geography": download_geography,
    "entities": download_entities,
    "versification": download_versification,
    "dictionaries": download_dictionaries,
    "commentaries": download_commentaries,
}


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="Download open-source Bible data into raw/")
    parser.add_argument(
        "--only",
        nargs="+",
        choices=list(DOWNLOAD_MAP.keys()),
        help="Download only specific data domains",
    )
    args = parser.parse_args()

    print("=" * 60)
    print("BibleStudio Data Pipeline — Downloader")
    print("=" * 60)

    ensure_dirs()

    targets = args.only if args.only else list(DOWNLOAD_MAP.keys())

    for name in targets:
        try:
            DOWNLOAD_MAP[name]()
        except requests.RequestException as e:
            print(f"\n  ⚠ Failed to download {name}: {e}", file=sys.stderr)
            print(f"    Skipping {name}, you can retry later with: python download.py --only {name}")
            continue
        except Exception as e:
            print(f"\n  ✗ Error in {name}: {e}", file=sys.stderr)
            continue

    print("\n" + "=" * 60)
    print("Download complete. Raw data is in: data-pipeline/raw/")
    print("Next step: python normalize.py")
    print("=" * 60)


if __name__ == "__main__":
    main()
