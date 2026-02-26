# Share Verse — Routes

> Integration points of the verse-sharing service with other modules.

---

## 1. No Own Routes

This service **has no own routes** and does not register as a `PaneType`. It is an injected service invoked from other modules.

---

## 2. Module Integration

### 2.1 Usage Pattern

```kotlin
// From any module that displays biblical text:
// val shareService: ShareVerseService by inject()
//
// // In the verse context menu:
// DropdownMenuItem(
//     text = { Text("Share verse") },
//     onClick = {
//         scope.launch {
//             shareService.shareVerseText(
//                 verseText = selectedVerse.text,
//                 reference = selectedVerse.reference,
//                 translationAbbr = currentTranslation.abbr,
//             )
//         }
//     },
// )
// DropdownMenuItem(
//     text = { Text("Share as image") },
//     onClick = {
//         scope.launch {
//             shareService.shareVerseImage(
//                 verseText = selectedVerse.text,
//                 reference = selectedVerse.reference,
//             )
//         }
//     },
// )
```

### 2.2 Integrated Modules

| Module | Integration widget | Trigger |
|--------|-------------------|---------|
| `bible-reader` | `VerseContextMenu` | Long press / right click on verse |
| `search` | `SearchResultTile` | Share button on each result |
| `highlights` | `HighlightListItem` | Highlight options menu |
| `note-editor` | `VerseQuoteBlock` | Quote options menu |
| `bookmarks-history` | `BookmarkTile` | Bookmark options menu |