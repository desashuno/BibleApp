# Timeline — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultTimelineComponent` | Scoped | Timeline browsing, era filtering, VerseBus |

---

## 2. TimelineComponent

### 2.1 Interface

```kotlin
interface TimelineComponent {
    val state: StateFlow<TimelineState>
    fun onLoad()
    fun onEraSelected(era: String?)
    fun onEventSelected(eventId: Long)
    fun onZoomChanged(level: ZoomLevel)
}

enum class ZoomLevel { Overview, Era, Year }
```

### 2.2 State

```kotlin
data class TimelineState(
    val loading: Boolean = false,
    val events: List<TimelineEvent> = emptyList(),
    val selectedEvent: TimelineEvent? = null,
    val eraFilter: String? = null,
    val zoomLevel: ZoomLevel = ZoomLevel.Overview,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content --> (zoom/filter) --> Content
                               +-> EventDetail
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB query | Load all events |
| `onEraSelected` | DB query | Filter by era |
| `onEventSelected` | DB query | Load event detail |
| VerseBus `VerseSelected` | DB query | Find events for verse |
| Verse ref tap | VerseBus publish | Navigate reader |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Subscribe + publish |
| `KnowledgeGraphComponent` | → Cross-link | Navigation | Events are entities too |

---

## 5. Component Registration (Koin)

```kotlin
val timelineModule = module {
    factory<TimelineComponent> { (ctx: ComponentContext) ->
        DefaultTimelineComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onEraSelected filters events`() = runTest {
    val repo = FakeTimelineRepository(events = testEvents)
    val component = DefaultTimelineComponent(TestComponentContext(), repo, VerseBus())
    component.onEraSelected("Patriarchs")
    component.state.test {
        assertThat(awaitItem().events.all { it.era == "Patriarchs" }).isTrue()
    }
}
```
