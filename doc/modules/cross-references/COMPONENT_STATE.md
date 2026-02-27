# Cross-References — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultCrossReferenceComponent` | Scoped (per pane) | `shared/.../features/crossreferences/component/DefaultCrossReferenceComponent.kt` | Cross-ref loading on verse selection |

---

## 2. CrossReferenceComponent

### 2.1 Interface

```kotlin
interface CrossReferenceComponent {
    val state: StateFlow<CrossReferenceState>
    fun onReferenceSelected(reference: CrossReference)
    fun onExpandReference(reference: CrossReference)
}
```

### 2.2 State

```kotlin
data class CrossReferenceState(
    val isLoading: Boolean = false,
    val sourceVerse: Int? = null,
    val references: List<CrossReference> = emptyList(),
    val parallels: List<ParallelPassage> = emptyList(),
    val expandedReferenceIds: Set<Long> = emptySet(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no verse selected)
  │
  │ VerseBus: VerseSelected received
  ▼
Loading (isLoading=true, sourceVerse set)
  │
  ├── success ──→ Content (references + parallels populated)
  │                │
  │                ├── onReferenceSelected(ref) ──→ Content (VerseBus published)
  │                ├── onExpandReference(ref) ──→ Content (inline text loaded)
  │                └── VerseBus: VerseSelected ──→ Loading (new verse)
  │
  └── failure ──→ Error (error set)
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | DB query | Loads cross-refs and parallels for verse |
| `onReferenceSelected` | VerseBus publish | Publishes `LinkEvent.VerseSelected(targetVerseId)` |
| `onExpandReference` | DB query | Loads full target verse text inline |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Receives | Decompose child | Lifecycle managed by workspace |
| `VerseBus` | ↔ Bidirectional | `SharedFlow<LinkEvent>` | Subscribes `VerseSelected`; publishes on ref tap |
| `BibleRepository` | → Reads | Koin DI | Loads target verse text for preview |

---

## 5. Component Registration (Koin)

```kotlin
val crossReferencesModule = module {
    factory<CrossReferenceComponent> { (componentContext: ComponentContext) ->
        DefaultCrossReferenceComponent(
            componentContext = componentContext,
            crossRefRepository = get(),
            parallelRepository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `VerseBus VerseSelected loads references`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultCrossReferenceComponent(
        componentContext = TestComponentContext(),
        crossRefRepository = FakeCrossRefRepository(testRefs),
        parallelRepository = FakeParallelRepository(),
        verseBus = verseBus,
    )
    component.state.test {
        verseBus.publish(LinkEvent.VerseSelected(globalVerseId = 01001001))
        assertThat(awaitItem().isLoading).isTrue()
        assertThat(awaitItem().references).isEqualTo(testRefs)
    }
}
```
