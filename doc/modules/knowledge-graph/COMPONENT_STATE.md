# Knowledge Graph — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultKnowledgeGraphComponent` | Scoped | Entity browsing, graph, VerseBus |

---

## 2. KnowledgeGraphComponent

### 2.1 Interface

```kotlin
interface KnowledgeGraphComponent {
    val state: StateFlow<KnowledgeGraphState>
    fun onLoad()
    fun onCategorySelected(type: EntityType)
    fun onEntitySelected(entityId: Long)
    fun onSearchChanged(query: String)
}
```

### 2.2 State

```kotlin
data class KnowledgeGraphState(
    val loading: Boolean = false,
    val selectedCategory: EntityType = EntityType.Person,
    val entities: List<Entity> = emptyList(),
    val selectedEntity: Entity? = null,
    val relationships: List<Relationship> = emptyList(),
    val searchQuery: String = "",
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content --> (category/search) --> Loading --> Content
                               +-> EntityDetail
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onCategorySelected` | DB query | Load entities by type |
| `onEntitySelected` | DB query | Load detail + relationships |
| VerseBus `VerseSelected` | DB query | Load entities for verse |
| Verse ref tap | VerseBus publish | Navigate reader to verse |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Subscribe + publish |
| `TheologicalAtlasComponent` | → Cross-link | Koin DI | Places link to atlas |
| `TimelineComponent` | → Cross-link | Koin DI | Events link to timeline |

---

## 5. Component Registration (Koin)

```kotlin
val knowledgeGraphModule = module {
    factory<KnowledgeGraphComponent> { (ctx: ComponentContext) ->
        DefaultKnowledgeGraphComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onCategorySelected loads entities`() = runTest {
    val repo = FakeKnowledgeGraphRepository(people = testPeople)
    val component = DefaultKnowledgeGraphComponent(TestComponentContext(), repo, VerseBus())
    component.onCategorySelected(EntityType.Person)
    component.state.test {
        assertThat(awaitItem().entities).isEqualTo(testPeople)
    }
}
```
