# Audio Sync — Roadmap

> Pending improvements, absorbed features, and effort estimates.

---

## 1. Pending Improvements

### P0 — Critical

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Audio timestamp schema | Create `audio_timestamps` table | 0.5 days | Phase 1 DB |
| 2 | `AudioSyncRepository` | Query timestamps per chapter/verse | 1 day | Schema |
| 3 | Platform audio player | expect/actual for Android/iOS/Desktop | 3 days | KMP |
| 4 | Sync engine | 100ms polling + verse mapping | 2 days | Repository + Player |
| 5 | Player UI | Play/pause/seek/speed controls | 2 days | Component |

### P1 — Important

| # | Improvement | Description | Estimated effort | Dependencies |
|---|------------|-------------|-----------------|-------------|
| 1 | Speed control | 0.5x / 1.0x / 1.5x / 2.0x | 0.5 days | Player |
| 2 | Background playback | Continue when app in background | 2 days | Platform |
| 3 | Audio import | Import audio files via module system | 1.5 days | Module system |

---

## 2. Comparison with Logos Bible Software

| Logos Feature | BibleStudio Status | Priority |
|--------------|-------------------|----------|
| Audio Bible playback | Planned | P0 |
| Verse-synced highlight | Planned | P0 |
| Speed control | P1 | P1 |
| Background playback | P1 | P1 |

---

## 3. Phase Estimates

| Phase | Total effort | Prerequisites |
|-------|-------------|--------------|
| **Phase 5** | 8.5 days | Phase 1 DB, KMP |
| **Phase 6** | 4 days | P0 complete |

---

## 4. Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-02-26 | Roadmap created | — |
