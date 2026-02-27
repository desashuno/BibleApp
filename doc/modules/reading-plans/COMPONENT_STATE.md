# Reading Plans — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultReadingPlansComponent` | Scoped | Plan selection, daily reading, progress |

---

## 2. ReadingPlansComponent

### 2.1 Interface

```kotlin
interface ReadingPlansComponent {
    val state: StateFlow<ReadingPlansState>
    fun onLoad()
    fun onActivatePlan(planId: Long)
    fun onMarkDayComplete(dayNumber: Int)
    fun onOpenPassage(startVerseId: Int, endVerseId: Int)
    fun onBrowsePlans()
}
```

### 2.2 State

```kotlin
data class ReadingPlansState(
    val loading: Boolean = false,
    val availablePlans: List<ReadingPlan> = emptyList(),
    val activePlan: ReadingPlan? = null,
    val currentDay: Int = 1,
    val progress: List<ReadingProgress> = emptyList(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> NoPlan --> BrowsePlans --> PlanActivated --> DailyReading
                                                                +-> AllComplete
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB query | Load active plan + progress |
| `onActivatePlan` | DB write | Set plan active + start date |
| `onMarkDayComplete` | DB insert | Record progress |
| `onOpenPassage` | VerseBus publish | Open Bible Reader to passage |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | → Publisher | SharedFlow | Open passage in reader |
| `BibleReaderComponent` | → Reads | Via VerseBus | Reader navigates to passage |

---

## 5. Component Registration (Koin)

```kotlin
val readingPlansModule = module {
    factory<ReadingPlansComponent> { (ctx: ComponentContext) ->
        DefaultReadingPlansComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onMarkDayComplete advances progress`() = runTest {
    val repo = FakeReadingPlanRepository(activePlan = testPlan)
    val component = DefaultReadingPlansComponent(TestComponentContext(), repo, VerseBus())
    component.onLoad()
    component.onMarkDayComplete(1)
    component.state.test {
        assertThat(awaitItem().progress).hasSize(1)
    }
}
```
