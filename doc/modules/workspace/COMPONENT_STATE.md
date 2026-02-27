# Workspace — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultWorkspaceComponent` | Global | `shared/.../features/workspace/component/DefaultWorkspaceComponent.kt` | Layout management, persistence |

---

## 2. WorkspaceComponent

### 2.1 Interface

```kotlin
interface WorkspaceComponent {
    val state: StateFlow<WorkspaceState>
    fun loadWorkspace(workspaceId: String)
    fun updateLayout(layout: LayoutNode)
    fun addPane(paneType: String)
    fun removePane(paneType: String)
    fun resizeSplit(path: List<Int>, newRatio: Float)
    fun movePane(from: List<Int>, to: List<Int>)
    fun switchTab(path: List<Int>, index: Int)
    fun applyPreset(preset: WorkspacePreset)
    fun saveWorkspace()
}
```

### 2.2 State

```kotlin
data class WorkspaceState(
    val layout: LayoutNode = LayoutNode.Leaf(paneType = "bible_reader"),
    val workspaceName: String = "Default",
    val loading: Boolean = false,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (loading=true)
  |
  | loadWorkspace(id)
  v
Loading
  |
  +-- success --> Active (layout populated)
  |                +-- addPane / removePane / resizeSplit --> Active
  |                +-- applyPreset --> Active (layout replaced)
  |                +-- saveWorkspace --> Active (persisted)
  +-- failure --> Error (fallback single pane)
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `loadWorkspace` | DB read | Loads layout JSON |
| `saveWorkspace` | DB write | Persists layout (debounced 2s) |
| Layout mutation | Auto-save | Triggers debounced save |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `RootComponent` | ← Parent | Decompose child | Lifecycle |
| All pane components | → Creates | PaneRegistry | Renders panes |

---

## 5. Component Registration (Koin)

```kotlin
val workspaceModule = module {
    singleOf(::WorkspaceRepositoryImpl) bind WorkspaceRepository::class
    single { PaneRegistry().apply { init() } }
    factory<WorkspaceComponent> { (componentContext: ComponentContext) ->
        DefaultWorkspaceComponent(componentContext = componentContext, repository = get(), paneRegistry = get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `loadWorkspace deserializes layout`() = runTest {
    val component = DefaultWorkspaceComponent(TestComponentContext(), FakeWorkspaceRepository(testLayout), FakePaneRegistry())
    component.state.test {
        component.loadWorkspace("test-uuid")
        assertThat(awaitItem().loading).isTrue()
        assertThat(awaitItem().layout).isInstanceOf(LayoutNode.Split::class)
    }
}
```
