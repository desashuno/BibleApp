# Resource Library — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultResourceLibraryComponent` | Scoped (per pane) | `shared/.../features/resources/component/DefaultResourceLibraryComponent.kt` | Resource browsing with verse selection |

---

## 2. ResourceLibraryComponent

### 2.1 Interface

```kotlin
interface ResourceLibraryComponent {
    val state: StateFlow<ResourceLibraryState>
    fun onResourceSelected(uuid: String)
    fun onVerseEntryTapped(entry: ResourceEntry)
}
```

### 2.2 State

```kotlin
data class ResourceLibraryState(
    val isLoading: Boolean = false,
    val resources: List<Resource> = emptyList(),
    val activeResourceId: String? = null,
    val entries: List<ResourceEntry> = emptyList(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no verse selected)
  |
  | VerseBus: VerseSelected
  v
Loading (isLoading=true)
  |
  +-- success --> Content (entries grouped by resource)
  |                |
  |                +-- onResourceSelected(uuid) --> Content (filtered entries)
  |                +-- VerseBus VerseSelected --> Loading (new verse)
  |
  +-- failure --> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | DB query | Loads resource entries for verse |
| `onResourceSelected` | State filter | Filters entries to selected resource |

---

## 4. Testing

```kotlin
@Test
fun `VerseSelected loads resource entries`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultResourceLibraryComponent(
        componentContext = TestComponentContext(),
        repository = FakeResourceRepository(testEntries),
        verseBus = verseBus,
    )
    component.state.test {
        verseBus.publish(LinkEvent.VerseSelected(01001001))
        val content = awaitItem { !it.isLoading }
        assertThat(content.entries).isNotEmpty()
    }
}
```
