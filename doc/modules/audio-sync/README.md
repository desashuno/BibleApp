# Audio Sync

> Documentation for the **audio-sync** module of BibleStudio.

---

## 1. Overview

Synchronized audio Bible playback with verse-level timing. Highlights the active verse in the Bible Reader pane as audio plays. Supports play, pause, skip to verse, playback speed control, and background playback on mobile platforms.

Audio timestamps are stored per-verse per-Bible to enable precise synchronization between the audio stream and on-screen text.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.audioSync` |
| **Category** | Media |
| **Accent color** | `#5A7A6B` (teal — paneMedia) |
| **Icon** | `Icons.headphones` |

---

## 3. Current Status

| Aspect | Status |
|--------|--------|
| SQLite schema | Defined (`audio_timestamps`) |
| Component(s) | Not started |
| UI / Pane | Not started |
| Tests | 0% |
| i18n | No |

---

## 4. Module Dependencies

### 4.1 Consumed (depends on)

| Module | Dependency type | Description |
|--------|----------------|-------------|
| `bible-reader` | Verse Bus | Syncs active verse highlight during playback |
| `module-system` | Data | Audio files are bundled in Bible modules |

### 4.2 Provided (depended on by)

| Module | Dependency type | Description |
|--------|----------------|-------------|
| `bible-reader` | Verse Bus | Reader highlights verse during audio playback |

---

## 5. Key Source Files

| File | Layer | Purpose |
|------|-------|---------|
| `shared/.../features/audiosync/component/DefaultAudioSyncComponent.kt` | Logic | Decompose component |
| `shared/.../features/audiosync/component/AudioSyncComponent.kt` | Logic | Component interface |
| `shared/.../features/audiosync/model/AudioTimestamp.kt` | Model | Domain entity |
| `shared/.../features/audiosync/repository/AudioSyncRepository.kt` | Data | Repository interface |
| `composeApp/.../features/audiosync/ui/AudioSyncPane.kt` | UI | Composable pane |

---

## Module Documents

| Document | Content |
|----------|---------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Exposed/consumed routes, Verse Bus, deep links |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories, queries |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes, responsive |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Decompose components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Prioritized pending improvements |
