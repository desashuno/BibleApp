# Theological Atlas — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Location schema + seed data | Create table + populate biblical locations | 2 days | Phase 1 DB |
| 2 | `AtlasRepository` | Query locations by verse, name | 1 day | Schema |
| 3 | Map renderer | Compose Canvas map with bundled tiles | 4 days | — |
| 4 | Markers + clustering | Location markers with zoom clustering | 2 days | Map |
| 5 | Location detail | Info + verse refs on marker tap | 1 day | Repository |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Journey routes | Draw polyline travel routes (Paul's journeys, Exodus, Conquest) with animated playback | 3 days | Map + route data |
| 2 | OSM tile caching | Persist downloaded tiles to disk for offline use | 1.5 days | OsmTileMap |
| 3 | Cross-link knowledge graph | Tap entity name in popup → navigate to Knowledge Graph pane | 1 day | Knowledge graph module |
| 4 | Verse-aware map | When a verse is selected in Bible Reader, auto-center map on related locations | 1 day | VerseBus + location_verse_index |
| 5 | Region boundary overlays | Draw translucent polygon overlays for biblical regions (Judah, Galilee, etc.) | 2 days | GeoJSON data |
| 6 | Timeline-synced map | Slider to filter locations by historical era; animate through time periods | 2 days | Timeline module |

### P2 — Desirable

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Satellite/terrain toggle | Switch between OSM standard, satellite, and terrain tile layers | 1 day | OsmTileMap |
| 2 | Location photo thumbnails | Show thumbnail images in popup cards (from Wikimedia Commons) | 2 days | Image loading |
| 3 | Distance measurement tool | Tap two points to see approximate distance in ancient units | 1 day | Map gestures |
| 4 | Export map view | Save current map view as PNG with pins and labels | 1 day | Canvas export |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority | Notes |
|--------------|-------------------|----------|-------|
| Bible atlas | ✅ Implemented (OSM tiles) | P0 | OpenStreetMap-based with 1,335 biblical locations |
| Location markers | ✅ Implemented | P0 | Color-coded by type, tap for detail popup |
| Journey routes | Planned | P1 | Polyline overlays with era filtering |
| Satellite view | Planned | P2 | Tile layer switching |
| Timeline sync | Planned | P1 | Era-based location filtering |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 10 days | Phase 1 DB |
| **Phase 6 (Data Integration)** | 4 days | Data pipeline seed DB |
| **Phase 7** | 6 days | P1 features |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
| 2026-02-28 | Added OSM tile map upgrade, data pipeline integration, P1/P2 enhancements | — |
