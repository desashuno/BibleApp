# Audio Sync — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  AudioSyncPane                                    |
|  AudioPlayer / SyncHighlight / SpeedControl       |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultAudioSyncComponent (Decompose)            |
|  +-- Manages StateFlow<AudioSyncState>            |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Audio playback via platform player           |
|  +-- Sync engine maps position to verse           |
+---------------------------------------------------+
|                      DATA                         |
|  AudioSyncRepository (interface)                  |
|  AudioSyncRepositoryImpl                          |
|  +-- AudioQueries (SQLDelight)                    |
|       +-- SQLite (audio_timestamps)               |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Synced Playback

1. **User navigates to passage** — VerseBus `VerseSelected` received.
2. **Load timestamps** — Query `audio_timestamps` for verse range.
3. **Audio play** — Platform audio player starts at verse offset.
4. **Sync loop** — Timer polls position; maps to current verse; highlights in reader.
5. **VerseBus publish** — Emits `VerseSelected` as audio advances.

### 2.2 Secondary Flows

- **Speed control** — 0.5x / 1.0x / 1.5x / 2.0x playback speed.
- **Manual seek** — User drags progress bar; snaps to nearest verse boundary.
- **Chapter navigation** — Previous/next chapter buttons.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Audio.sq` | `timestampsForChapter` | `bibleId, bookNum, chapterNum` | `List<AudioTimestamp>` | All verse timings |
| `Audio.sq` | `timestampForVerse` | `bibleId, globalVerseId` | `AudioTimestamp?` | Single verse timing |
| `Audio.sq` | `audioFileForChapter` | `bibleId, bookNum, chapterNum` | `String?` | Audio file path |

---

## 4. Dependency Injection

```kotlin
val audioSyncModule = module {
    singleOf(::AudioSyncRepositoryImpl) bind AudioSyncRepository::class
    single { PlatformAudioPlayer() }
    factory { (ctx: ComponentContext) ->
        DefaultAudioSyncComponent(ctx, get(), get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `AudioSyncRepositoryImpl` | Abstracts timestamp queries |
| Observer | VerseBus bidirectional | Receives verse; publishes as audio advances |
| Platform abstraction | `PlatformAudioPlayer` | expect/actual for Android/iOS/Desktop |
| Sync engine | Polling timer | Maps audio position to verse timestamps |

---

## 6. Performance Considerations

- **Timestamp load < 5 ms** — Indexed per chapter.
- **Sync polling** — 100ms interval; lightweight position check.
- **Audio buffering** — Platform player handles buffering; app only manages sync.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Polling sync (100ms) | Callback-based | Cross-platform consistency; simple implementation |
| Per-chapter audio files | Per-book / per-verse | Balance between file count and granularity |
| Platform audio player | Custom decoder | Leverage OS audio stack; codec support |
