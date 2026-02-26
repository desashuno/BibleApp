# {Module Name} — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `Default{Module}Component` | {Global / Scoped} | `shared/.../features/{module}/component/Default{Module}Component.kt` | {main description} |

---

## 2. {Module}Component

### 2.1 Interface

```kotlin
// interface {Module}Component {
//     val state: StateFlow<{Module}State>
//     fun onLoad()
//     fun onFilterChanged(filter: String)
//     fun onItemSelected(itemId: Long)
// }
```

### 2.2 State

```kotlin
// data class {Module}State(
//     val loading: Boolean = false,
//     val items: List<{Entity}> = emptyList(),
//     val selectedItem: {Entity}? = null,
//     val error: AppError? = null,
// )
```

| Field | Type | Description |
|-------|------|-------------|
| `loading` | `Boolean` | Whether data is being fetched |
| `items` | `List<{Entity}>` | Loaded data items |
| `selectedItem` | `{Entity}?` | Currently selected item, or null |
| `error` | `AppError?` | Error state, or null |

### 2.3 State Transitions

```
Initial (loading=false, empty)
  │
  │ onLoad()
  ▼
Loading (loading=true)
  │
  ├── success ──→ Content (items populated)
  │                │
  │                ├── onFilterChanged() ──→ Loading ──→ Content
  │                ├── onItemSelected() ──→ Content (selectedItem set)
  │                └── onLoad() (refresh) ──→ Loading
  │
  └── failure ──→ Error (error set)
                   │
                   └── onLoad() (retry) ──→ Loading
```

---

## 3. Side Effects

<!-- Side effects the component triggers (Verse Bus publish, navigation, snackbar, etc.). -->

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onItemSelected` | Verse Bus publish | Publishes `globalVerseId` to VerseBus |
| error catch | Logging | Logs error via Napier |

---

## 4. Interaction with Other Components

<!-- How this component communicates with components in other modules. -->

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Receives | Decompose child | Lifecycle managed by workspace |
| `VerseBus` | ↔ Bidirectional | SharedFlow | Active verse synchronization |

---

## 5. Component Registration (Koin)

<!-- How the component is registered and resolved in the DI container. -->

```kotlin
// In the module's Koin module:
// val {module}Module = module {
//     factory<{Module}Component> { (componentContext: ComponentContext) ->
//         Default{Module}Component(
//             componentContext = componentContext,
//             repository = get(),
//             verseBus = get(),
//         )
//     }
// }
```

---

## 6. Testing

<!-- Testing strategy for the component. -->

```kotlin
// @Test
// fun `onLoad emits loading then content`() = runTest {
//     val component = Default{Module}Component(
//         componentContext = TestComponentContext(),
//         repository = FakeRepository(testItems),
//         verseBus = VerseBus(),
//     )
//     component.state.test {
//         component.onLoad()
//         assertThat(awaitItem().loading).isTrue()
//         assertThat(awaitItem().items).isEqualTo(testItems)
//     }
// }
```
