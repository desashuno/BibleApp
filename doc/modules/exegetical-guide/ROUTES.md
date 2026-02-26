# {Module Name} — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.
> Modules communicate exclusively through Verse Bus events, Decompose child navigation, and config-based deep links.

---

## 1. Exposed Configurations

<!-- Decompose configurations that allow other components to navigate to this module. -->

### 1.1 Configuration Table

| Config | Parameters | Description |
|--------|-----------|-------------|
| `{Module}Config.Main` | — | Main view of the module |
| `{Module}Config.Detail(id)` | `id: Long` | Detail view for a specific resource |

### 1.2 Config Definition

```kotlin
// sealed class {Module}Config : Parcelable {
//     @Parcelize data object Main : {Module}Config()
//     @Parcelize data class Detail(val id: Long) : {Module}Config()
// }
```

---

## 2. Consumed Configurations

<!-- Configurations from other modules that this module navigates to. -->

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `{other-module}` | `{OtherModule}Config.Detail(id)` | `id: Long` | {When and why navigation occurs} |

### 2.1 Navigation Examples

```kotlin
// Navigate to Word Study from this module
// navigation.push(WordStudyConfig.Detail(strongsNumber = "H1234"))

// Open a passage in the Bible Reader
// verseBus.publish(LinkEvent.VerseSelected(globalVerseId = 01001001))
```

---

## 3. Pane Opening (Workspace)

<!-- How this module is opened as a pane in the multi-pane workspace. -->

### 3.1 PaneRegistry Key

```kotlin
// PaneRegistry.build("{module}", config = mapOf("{key}" to "{value}"))
```

### 3.2 Pane Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `{key}` | `{type}` | {Yes/No} | {description} |

---

## 4. Deep Links

<!-- Deep link format for accessing this module's content externally. -->

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://{module}/{id}` | `biblestudio://{module}/123` | Opens detail view for resource 123 |

---

## 5. Verse Bus (LinkEvent)

<!-- If the module participates in the Verse Bus for cross-pane verse synchronization. -->

| Role | Description |
|------|-------------|
| {Publisher / Subscriber / Both} | {What verse event it publishes or reacts to} |

### 5.1 Publishing

```kotlin
// When the user selects a verse in this module
// verseBus.publish(LinkEvent.VerseSelected(globalVerseId = verseId))
```

### 5.2 Subscribing

```kotlin
// When another module publishes a verse, this module reacts
// scope.launch {
//     verseBus.events.collect { event ->
//         when (event) {
//             is LinkEvent.VerseSelected -> navigateToVerse(event.globalVerseId)
//             else -> { /* ignore */ }
//         }
//     }
// }
```

---

## 6. Inter-Module Communication Diagram

```
┌─────────────┐  config: WordStudyConfig.Detail  ┌──────────────┐
│             │ ──────────────────────────────── → │              │
│  {Module}   │                                    │  Word Study  │
│             │ ← ─────────────────────────────── │              │
└─────────────┘  VerseBus: VerseSelected           └──────────────┘
       │
       │ VerseBus: VerseSelected
       ▼
┌──────────────┐
│ Bible Reader │
└──────────────┘
```

<!-- Adapt the diagram to the actual routes and communications of this module. -->
