# Audio Sync — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("audio_sync") { config -> AudioSyncPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `audio_sync` |
| **Category** | Media |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `AudioSyncPane` | Main pane with player controls | No |
| `AudioPlayer` | Play/pause/seek controls | No |
| `SyncHighlight` | Verse highlight sync indicator | No |
| `SpeedControl` | Playback speed selector | Yes |
| `ChapterSelector` | Chapter navigation arrows | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [A] Audio Sync                  [v] [x]  |
+------------------------------------------+
| KJV  |  Genesis 1                        |
+------------------------------------------+
|                                          |
| Now reading: Genesis 1:3                 |
| "And God said, Let there be light..."    |
|                                          |
+------------------------------------------+
| [|<]  [<<]  [ > || ]  [>>]  [>|]        |
| |===========*---------| 1:23 / 4:15      |
|                                          |
| Speed: [0.5x] [1.0x] [1.5x] [2.0x]      |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Floating mini-player at bottom |
| **Tablet** | Compact strip below reader |
| **Desktop** | Workspace pane or floating player |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Seeks audio to verse start position |
| Audio advances to new verse | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **No audio** | "No audio available for this Bible" | No timestamps found |
| **Ready** | Play button enabled | Timestamps loaded |
| **Playing** | Animated equalizer + verse tracking | Play pressed |
| **Paused** | Pause icon + static position | Pause pressed |
| **Loading** | Buffering spinner | Audio loading |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Player controls | Standard media control semantics |
| Progress | "Position: [time] of [total]" |
| Speed | Announced on change: "Playback speed: [x]" |
| Current verse | Screen reader announces verse as audio advances |
