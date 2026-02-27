# Dashboard — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultDashboardComponent` | Scoped | Widget data aggregation |

---

## 2. DashboardComponent

### 2.1 Interface

```kotlin
interface DashboardComponent {
    val state: StateFlow<DashboardState>
    fun onLoad()
    fun onOpenReadingPlan()
    fun onOpenNote(noteId: Long)
    fun onBookmarkTapped(globalVerseId: Int)
    fun onWorkspaceSelected(workspaceId: Long)
}
```

### 2.2 State

```kotlin
data class DashboardState(
    val loading: Boolean = false,
    val data: DashboardData? = null,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content (widgets populated)
                   +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | 4 parallel DB queries | Load all widget data |
| `onBookmarkTapped` | VerseBus publish | Navigate reader to verse |
| `onOpenNote` | Navigation | Open note editor pane |
| `onOpenReadingPlan` | Navigation | Open reading plans pane |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | → Publisher | SharedFlow | Navigate to verse |
| `WorkspaceComponent` | ← Parent | Decompose | Open other panes |

---

## 5. Component Registration (Koin)

```kotlin
val dashboardModule = module {
    factory<DashboardComponent> { (ctx: ComponentContext) ->
        DefaultDashboardComponent(ctx, get(), get(), get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onLoad populates all widgets`() = runTest {
    val component = DefaultDashboardComponent(
        TestComponentContext(),
        FakeReadingPlanRepo(), FakeNoteRepo(),
        FakeBookmarkRepo(), FakeWorkspaceRepo()
    )
    component.onLoad()
    component.state.test {
        val data = awaitItem().data
        assertThat(data).isNotNull()
        assertThat(data!!.recentNotes).isNotEmpty()
    }
}
```
