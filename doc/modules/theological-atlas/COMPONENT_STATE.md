# Theological Atlas — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultAtlasComponent` | Scoped | Map browsing, location detail, VerseBus |

---

## 2. AtlasComponent

### 2.1 Interface

```kotlin
interface AtlasComponent {
    val state: StateFlow<AtlasState>
    fun onLoad()
    fun onLocationSelected(locationId: Long)
    fun onSearchChanged(query: String)
    fun onViewChanged(view: AtlasView)
}
enum class AtlasView { Map, List }
```

### 2.2 State

```kotlin
data class AtlasState(
    val loading: Boolean = false,
    val locations: List<Location> = emptyList(),
    val selectedLocation: Location? = null,
    val view: AtlasView = AtlasView.Map,
    val searchQuery: String = "",
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content (map/list) --> LocationDetail
                   +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB query | Load all locations |
| `onLocationSelected` | DB query | Load detail + verse refs |
| VerseBus `VerseSelected` | DB query | Highlight verse's locations |
| Verse ref tap | VerseBus publish | Navigate reader |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Subscribe + publish |
| `KnowledgeGraphComponent` | → Cross-link | Navigation | Places ↔ entities |

---

## 5. Component Registration (Koin)

```kotlin
val atlasModule = module {
    factory<AtlasComponent> { (ctx: ComponentContext) ->
        DefaultAtlasComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onLocationSelected shows detail`() = runTest {
    val repo = FakeAtlasRepository(locations = testLocations)
    val component = DefaultAtlasComponent(TestComponentContext(), repo, VerseBus())
    component.onLocationSelected(1L)
    component.state.test {
        assertThat(awaitItem().selectedLocation?.name).isEqualTo("Jerusalem")
    }
}
```
