# Bookmarks & History — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultBookmarksComponent` | Scoped | Bookmark CRUD, history, folder management |

---

## 2. BookmarksComponent

### 2.1 Interface

```kotlin
interface BookmarksComponent {
    val state: StateFlow<BookmarksState>
    fun onLoad()
    fun onCreateBookmark(globalVerseId: Int, label: String, folderId: String?)
    fun onDeleteBookmark(uuid: String)
    fun onBookmarkSelected(bookmark: Bookmark)
    fun onCreateFolder(name: String)
    fun onTabChanged(tab: BookmarksTab)
}
enum class BookmarksTab { Bookmarks, History }
```

### 2.2 State

```kotlin
data class BookmarksState(
    val loading: Boolean = false,
    val activeTab: BookmarksTab = BookmarksTab.Bookmarks,
    val folders: List<BookmarkFolder> = emptyList(),
    val bookmarks: Map<String?, List<Bookmark>> = emptyMap(),
    val history: List<HistoryEntry> = emptyList(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content (bookmarks + folders)
                   +-> Error
Content --> Create/Delete --> Content (refreshed)
Content --> Tab switch --> Content (History tab)
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onCreateBookmark` | DB insert | Persist new bookmark |
| `onBookmarkSelected` | VerseBus publish | Navigate reader to verse |
| VerseBus `VerseSelected` | History record | Auto-add to history |

---

## 4. Component Registration (Koin)

```kotlin
val bookmarksModule = module {
    factory<BookmarksComponent> { (ctx: ComponentContext) ->
        DefaultBookmarksComponent(ctx, get(), get())
    }
}
```

---

## 5. Testing

```kotlin
@Test
fun `onCreateBookmark adds to folder`() = runTest {
    val component = DefaultBookmarksComponent(TestComponentContext(), FakeBookmarkRepository(), VerseBus())
    component.onCreateBookmark(01001001, "Genesis 1:1", "study-folder")
    assertThat(component.state.value.bookmarks["study-folder"]).hasSize(1)
}
```
