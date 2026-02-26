# Share Verse — Shared Service

> Documentation for the **Share Verse** shared service in BibleStudio.

---

## 1. Overview

Cross-cutting service that generates shareable verse images and shares them through native platform APIs (Share Sheet on iOS/Android, clipboard on desktop). Accessible from any module that displays biblical text.

---

## 2. Component Type

| Field | Value |
|-------|-------|
| **Type** | Shared service (not a standalone pane/module) |
| **Access** | Context menu when selecting verse(s) in any module |
| **Platforms** | All (Android, iOS, Windows, macOS, Linux) |

---

## 3. Features

| Feature | Description |
|---------|-------------|
| **Copy text** | Copies the verse text to the clipboard with reference |
| **Share text** | Opens the native share sheet with the verse text |
| **Generate image** | Creates a styled verse image with background, typography, and branding |
| **Share image** | Shares the generated image via share sheet or saves to gallery |
| **Templates** | Multiple design templates for images |

---

## 4. Modules that Consume this Service

| Module | Integration point |
|--------|-------------------|
| `bible-reader` | Context menu when selecting text |
| `search` | Share button in search results |
| `highlights` | Share highlighted verse |
| `note-editor` | Share verse quote within a note |
| `bookmarks-history` | Share bookmark |

---

## 5. Service API

```kotlin
// interface ShareVerseService {
//     /** Copies the verse text to the clipboard */
//     suspend fun copyVerseText(
//         verseText: String,
//         reference: String,
//         translationAbbr: String? = null,
//     )
//
//     /** Opens the native share sheet with text */
//     suspend fun shareVerseText(
//         verseText: String,
//         reference: String,
//         translationAbbr: String? = null,
//     )
//
//     /** Generates a verse image */
//     suspend fun generateVerseImage(
//         verseText: String,
//         reference: String,
//         translationAbbr: String? = null,
//         template: VerseImageTemplate = VerseImageTemplate.CLASSIC,
//     ): ByteArray
//
//     /** Shares the verse image */
//     suspend fun shareVerseImage(
//         verseText: String,
//         reference: String,
//         translationAbbr: String? = null,
//         template: VerseImageTemplate = VerseImageTemplate.CLASSIC,
//     )
// }
```

---

## Service Documents

| Document | Contents |
|----------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture and platform dependencies |
| [ROUTES.md](ROUTES.md) | Integration points with other modules |
| [DATA_MODEL.md](DATA_MODEL.md) | Templates and configurations |