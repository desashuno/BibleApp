# Share Verse — Architecture

> Architecture of the shared verse-sharing service.

---

## 1. Layer Diagram

```
+----------------------------------------------+
|       CONSUMING MODULES                       |
|  BibleReader / Search / Highlights / ...     |
|  +-- Invoke ShareVerseService                |
+----------------------------------------------+
|            SHARE VERSE SERVICE                |
|  ShareVerseService (interface)               |
|  +-- ShareVerseServiceImpl                   |
|       +-- TextFormatter (text formatting)    |
|       +-- ImageGenerator (image rendering)   |
|       +-- PlatformSharer (native share sheet)|
+----------------------------------------------+
|            PLATFORM LAYER                     |
|  +-- expect/actual (share sheet)             |
|  +-- Clipboard API                           |
|  +-- Canvas (image generation)               |
+----------------------------------------------+
```

---

## 2. Dependencies

| Dependency | Type | Usage |
|------------|------|-------|
| `expect/actual` PlatformSharer | KMP | Native multiplatform share sheet |
| `okio` / platform temp dir | Library | Temporary directory for images |
| Platform Clipboard API | SDK | Clipboard access |

---

## 3. Dependency Injection

```kotlin
// val shareModule = module {
//     singleOf(::ShareVerseServiceImpl) bind ShareVerseService::class
// }
```

---

## 4. Platform Considerations

| Platform | Behavior |
|----------|----------|
| **Android/iOS** | Native share sheet with text or image |
| **Windows/macOS/Linux** | Clipboard + option to save image |