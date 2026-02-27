# Module System — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultModuleSystemComponent` | Scoped | Module browsing, install, uninstall |

---

## 2. ModuleSystemComponent

### 2.1 Interface

```kotlin
interface ModuleSystemComponent {
    val state: StateFlow<ModuleSystemState>
    fun onLoad()
    fun onModuleSelected(moduleId: String)
    fun onInstallFromFile(fileBytes: ByteArray, fileName: String)
    fun onUninstall(moduleId: String)
    fun onSearchChanged(query: String)
}
```

### 2.2 State

```kotlin
data class ModuleSystemState(
    val loading: Boolean = false,
    val modules: List<InstalledModule> = emptyList(),
    val selectedModule: InstalledModule? = null,
    val installProgress: Float? = null,
    val searchQuery: String = "",
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content --> Installing --> Content
                   |                           +-> Error
                   +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB read | Loads installed modules |
| `onInstallFromFile` | File I/O + DB write | Validate, extract, import |
| `onUninstall` | DB delete (transaction) | Remove module data |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Parent | Decompose child | Lifecycle |
| `BibleRepository` | → Writes | Koin DI | Bible data import |
| `ResourceRepository` | → Writes | Koin DI | Resource import |

---

## 5. Component Registration (Koin)

```kotlin
val moduleSystemModule = module {
    factory<ModuleSystemComponent> { (ctx: ComponentContext) ->
        DefaultModuleSystemComponent(ctx, get(), get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onLoad populates installed modules`() = runTest {
    val component = DefaultModuleSystemComponent(TestComponentContext(), FakeModuleRepository(testModules), ModuleValidator(), FakeImporter())
    component.state.test {
        component.onLoad()
        assertThat(awaitItem().loading).isTrue()
        assertThat(awaitItem().modules).isEqualTo(testModules)
    }
}
```
