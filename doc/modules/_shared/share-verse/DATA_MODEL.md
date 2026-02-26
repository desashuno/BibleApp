# Share Verse — Data Model

> Templates and configurations for the verse-sharing service.

---

## 1. Entities

### 1.1 VerseImageTemplate

```kotlin
// enum class VerseImageTemplate {
//     CLASSIC,     // Vellum background with Crimson Pro typography
//     MODERN,      // Gradient background with Inter typography
//     MINIMAL,     // White background with serif typography
//     DARK,        // Dark background with light typography
//     SCRIPTURE,   // Illuminated manuscript style
// }
```

### 1.2 ShareResult

```kotlin
// data class ShareResult(
//     val success: Boolean,
//     val errorMessage: String? = null,
//     val method: ShareMethod, // TEXT, IMAGE, CLIPBOARD
// )
```

---

## 2. SQLite Schema

This service **has no own tables**. It uses data from consuming modules (verse text, references).

---

## 3. Persisted Configurations

User preferences (favorite template, reference format) are stored in the `settings` table of the Settings module:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `share_default_template` | `String` | `'classic'` | Default template for images |
| `share_include_translation` | `Boolean` | `true` | Include translation abbreviation |
| `share_reference_format` | `String` | `'short'` | Reference format (short/long) |