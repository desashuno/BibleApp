# Audio Sync — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultAudioSyncComponent` | Scoped | Audio playback, verse sync, VerseBus |

---

## 2. AudioSyncComponent

### 2.1 Interface

```kotlin
interface AudioSyncComponent {
    val state: StateFlow<AudioSyncState>
    fun onLoad(bibleId: Long, globalVerseId: Int)
    fun onPlay()
    fun onPause()
    fun onSeek(positionMs: Long)
    fun onSpeedChanged(speed: Float)
    fun onNextChapter()
    fun onPrevChapter()
}
```

### 2.2 State

```kotlin
data class AudioSyncState(
    val loading: Boolean = false,
    val playing: Boolean = false,
    val currentVerseId: Int? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1.0f,
    val audioAvailable: Boolean = false,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Ready --> Playing --> Paused --> Playing
                   +-> NoAudio               +-> Seeking --> Playing
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB query + audio init | Load timestamps + prepare player |
| `onPlay` | Platform audio | Start/resume playback |
| `onPause` | Platform audio | Pause playback |
| Sync timer | VerseBus publish | Emit `VerseSelected` as audio advances |
| VerseBus `VerseSelected` | Seek | Jump to verse position |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Receive verse → seek; publish as audio advances |
| `PlatformAudioPlayer` | → Controls | expect/actual | Platform audio API |
| `BibleReaderComponent` | Via VerseBus | SharedFlow | Highlight current verse in reader |

---

## 5. Component Registration (Koin)

```kotlin
val audioSyncModule = module {
    factory<AudioSyncComponent> { (ctx: ComponentContext) ->
        DefaultAudioSyncComponent(ctx, get(), get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `sync timer emits verse changes`() = runTest {
    val player = FakeAudioPlayer()
    val component = DefaultAudioSyncComponent(TestComponentContext(), FakeAudioRepo(testTimestamps), player, VerseBus())
    component.onLoad(1L, 01001001)
    component.onPlay()
    player.advanceTo(5000) // 5 seconds
    component.state.test {
        assertThat(awaitItem().currentVerseId).isEqualTo(01001003) // verse 3 at 5s
    }
}
```
