# Design System

> BibleStudio — Visual Identity, Theming, Components & Accessibility

---

## 1. Identity: Digital Scriptorium

BibleStudio follows the **Digital Scriptorium** design language — a visual system inspired by traditional manuscript aesthetics translated into a modern, functional interface. The identity balances scholarly gravitas with clean, accessible digital design.

### Design Principles

1. **Readability first** — Bible text is the hero. Every design decision defers to legibility.
2. **Calm focus** — Muted tones, generous whitespace, minimal visual noise.
3. **Scholarly precision** — Clear typographic hierarchy, consistent alignment, structured layouts.
4. **Accessible by default** — WCAG 2.1 AA contrast, screen reader support, keyboard navigation.
5. **Adaptive fidelity** — Desktop exploits space; mobile distills to essentials.

---

## 2. Color Palette

### 2.1 Primary

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `primary` | `#5B4A3F` | `#C4A882` | App bar, primary actions, active states |
| `primaryVariant` | `#3E3229` | `#D4BC9A` | Pressed states, emphasis |
| `onPrimary` | `#FFFFFF` | `#1A1410` | Text/icons on primary surfaces |
| `surface` | `#FAF8F5` | `#1E1B18` | Page backgrounds |
| `onSurface` | `#2C2520` | `#E8E0D8` | Body text |
| `surfaceVariant` | `#F0EDE8` | `#2A2520` | Card backgrounds, secondary surfaces |

### 2.2 Pane Categories

Each module category has a distinct accent for visual differentiation in the workspace:

| Category | Color | Token | Example Modules |
|----------|-------|-------|-----------------|
| Bible Text | `#5B7E6E` (sage) | `paneText` | Bible Reader, Text Comparison |
| Study | `#6B5B8A` (muted purple) | `paneStudy` | Word Study, Morphology, Cross-Refs |
| Resources | `#7A6B5A` (warm brown) | `paneResource` | Commentary, Dictionary, Passage Guide |
| Writing | `#5A6B7A` (steel blue) | `paneWriting` | Sermon Editor, Note Editor |
| Tools | `#7A5A5A` (dusty rose) | `paneTool` | Search, Timeline, Atlas |
| Media | `#5A7A6B` (teal) | `paneMedia` | Audio Sync, Visual Copy |

### 2.3 Semantic Colors

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `error` | `#B3261E` | `#F2B8B5` | Validation errors, destructive actions |
| `success` | `#2E7D32` | `#81C784` | Save confirmations, import success |
| `warning` | `#E65100` | `#FFB74D` | Unsaved changes, sync conflicts |
| `info` | `#0277BD` | `#4FC3F7` | Tips, informational banners |

### 2.4 Highlight Colors

Users assign highlight colors to verses. The palette provides 8 presets:

| Index | Name | Hex | Preview |
|-------|------|-----|---------|
| 0 | Yellow | `#FFF3B0` | █ |
| 1 | Green | `#C8E6C9` | █ |
| 2 | Blue | `#BBDEFB` | █ |
| 3 | Pink | `#F8BBD0` | █ |
| 4 | Orange | `#FFE0B2` | █ |
| 5 | Purple | `#E1BEE7` | █ |
| 6 | Red | `#FFCDD2` | █ |
| 7 | Teal | `#B2DFDB` | █ |

All highlight colors maintain WCAG AA contrast ratio (≥ 4.5:1) against `onSurface` text in both light and dark themes.

---

## 3. Typography

### 3.1 Font Families

| Family | Usage | Weight Range |
|--------|-------|-------------|
| **Merriweather** | Bible text, headings | 300–900 |
| **Source Sans 3** | UI text, labels, controls | 300–700 |
| **JetBrains Mono** | Strong's numbers, morphology codes, debug | 400–600 |

All fonts are bundled as Compose Multiplatform resources — no network dependency.

### 3.2 Type Scale

| Role | Family | Size | Weight | Line Height | Usage |
|------|--------|------|--------|-------------|-------|
| `displayLarge` | Merriweather | 32 | 700 | 1.3 | Book titles |
| `displayMedium` | Merriweather | 28 | 700 | 1.3 | Chapter headers |
| `headlineLarge` | Merriweather | 24 | 600 | 1.35 | Pane titles |
| `headlineMedium` | Source Sans 3 | 20 | 600 | 1.4 | Section headers |
| `titleLarge` | Source Sans 3 | 18 | 600 | 1.4 | Card titles |
| `titleMedium` | Source Sans 3 | 16 | 500 | 1.4 | Subtitles |
| `bodyLarge` | Merriweather | 18 | 400 | 1.6 | Bible text (default) |
| `bodyMedium` | Source Sans 3 | 14 | 400 | 1.5 | UI body text |
| `bodySmall` | Source Sans 3 | 12 | 400 | 1.5 | Captions, metadata |
| `labelLarge` | Source Sans 3 | 14 | 600 | 1.4 | Buttons, tabs |

### 3.3 Bible Text Scaling

Users can adjust Bible text size independently of the system font scale. The `bodyLarge` role (Bible text) scales from **14 sp** to **28 sp** via a Settings slider. The scale factor is stored in `SettingsRepository` and applied via a `CompositionLocal` provider.

---

## 4. Theme Implementation

### 4.1 AppTheme

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF5B4A3F),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFAF8F5),
    onSurface = Color(0xFF2C2520),
    surfaceVariant = Color(0xFFF0EDE8),
    error = Color(0xFFB3261E),
)

private val darkColorScheme = darkColorScheme(
    primary = Color(0xFFC4A882),
    onPrimary = Color(0xFF1A1410),
    surface = Color(0xFF1E1B18),
    onSurface = Color(0xFFE8E0D8),
    surfaceVariant = Color(0xFF2A2520),
    error = Color(0xFFF2B8B5),
)
```

### 4.2 Typography

```kotlin
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (32 * 1.3).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (28 * 1.3).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (24 * 1.35).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (20 * 1.4).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (18 * 1.6).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (14 * 1.5).sp,
    ),
    bodySmall = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (12 * 1.5).sp,
    ),
    labelLarge = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (14 * 1.4).sp,
    ),
)
```

### 4.3 Shapes

```kotlin
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
)
```

### 4.4 Theme Switching

Theme mode is stored in `SettingsRepository` with three options: `light`, `dark`, `system`. The root composable reads system brightness via `isSystemInDarkTheme()` when set to `system`:

```kotlin
@Composable
fun BibleStudioApp(settingsRepository: SettingsRepository) {
    val themeMode by settingsRepository.watchThemeMode().collectAsState(ThemeMode.System)
    val darkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    AppTheme(darkTheme = darkTheme) {
        // ...
    }
}
```

---

## 5. Animation

### 5.1 Duration Constants

| Token | Duration | Usage |
|-------|----------|-------|
| `DurationFast` | 150 ms | Hover, focus, icon toggles |
| `DurationMedium` | 250 ms | Page transitions, pane resize |
| `DurationSlow` | 400 ms | Layout shifts, workspace transitions |

```kotlin
object AnimationDurations {
    const val Fast = 150
    const val Medium = 250
    const val Slow = 400
}
```

### 5.2 Easing

| Token | Easing | Usage |
|-------|--------|-------|
| `EaseStandard` | `FastOutSlowInEasing` | General transitions |
| `EaseDecelerate` | `LinearOutSlowInEasing` | Elements entering view |
| `EaseAccelerate` | `FastOutLinearInEasing` | Elements leaving view |

### 5.3 Compose Animation APIs

```kotlin
// State-driven animation
val alpha by animateFloatAsState(
    targetValue = if (isSelected) 1f else 0.6f,
    animationSpec = tween(durationMillis = AnimationDurations.Fast),
)

// Visibility
AnimatedVisibility(
    visible = showPanel,
    enter = fadeIn(tween(AnimationDurations.Medium)) + expandVertically(),
    exit = fadeOut(tween(AnimationDurations.Medium)) + shrinkVertically(),
) {
    PanelContent()
}
```

### 5.4 Reduced Motion

When the platform accessibility setting requests reduced motion, all durations collapse to 0 ms. The app checks this via `expect`/`actual`:

```kotlin
// commonMain
expect val reduceMotion: Boolean

// androidMain
actual val reduceMotion: Boolean
    get() = Settings.Global.getFloat(
        contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE, 1f
    ) == 0f

// desktopMain (JVM)
actual val reduceMotion: Boolean
    get() = System.getProperty("javax.accessibility.reduceMotion")?.toBoolean() ?: false
```

---

## 6. Components

### 6.1 PaneContainer

The primary wrapper for every module in the workspace layout:

```
┌─────────────────────────────────────────┐
│ [icon] Pane Title              [⋮] [✕] │  ← Header (36px)
├─────────────────────────────────────────┤
│                                         │
│           Module Content                │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

```kotlin
@Composable
fun PaneContainer(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClose: () -> Unit,
    onMenuAction: (PaneAction) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PaneHeader(
            title = title,
            icon = icon,
            accentColor = accentColor,
            onClose = onClose,
            onMenuAction = onMenuAction,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            content()
        }
    }
}
```

- **Header**: 36 dp height. Displays pane category icon (colored), title, overflow menu, and close button.
- **Drag handle**: The header is a drag target for workspace rearrangement via `Modifier.draggable`.
- **Overflow menu**: Collapse, maximize, move, split, settings.
- **Focus ring**: 2 dp `primary` border when the pane has keyboard focus.

### 6.2 VerseComposable

Renders a single verse with interactive capabilities:

```kotlin
@Composable
fun VerseItem(
    verse: Verse,
    isSelected: Boolean,
    highlight: HighlightColor?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        highlight != null -> highlight.color.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics { contentDescription = "Verse ${verse.verseNumber}: ${verse.text}" },
    ) {
        Text(
            text = "${verse.verseNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(28.dp).alignByBaseline(),
        )
        Text(
            text = verse.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.alignByBaseline(),
        )
    }
}
```

- Verse number rendered in `bodySmall` with `primary` color.
- Text rendered in `bodyLarge` (Merriweather).
- Selected verse gets a subtle `primaryContainer` background.
- Highlighted verses show their highlight color as background.
- Long-press opens a context menu: Copy, Highlight, Note, Bookmark, Share.

### 6.3 ActivityBar (Desktop)

Vertical bar on the left edge of the workspace. 48 dp wide.

```
┌────┐
│ 📖 │  Bible Reader
│ 🔍 │  Search
│ 📝 │  Notes
│ 📚 │  Resources
│    │
│    │  (spacer)
│    │
│ ⚙️ │  Settings
└────┘
```

- Icons use the pane category color.
- Active pane's icon has a `primary` background indicator (4 dp line).
- Tooltip delay: 500 ms via `TooltipBox`.

### 6.4 BottomNav (Mobile)

Bottom navigation bar on phones. 5 slots maximum.

| Slot | Default Module | Icon |
|------|---------------|------|
| 1 | Bible Reader | `Icons.AutoMirrored.Rounded.MenuBook` |
| 2 | Search | `Icons.Rounded.Search` |
| 3 | Notes | `Icons.Rounded.EditNote` |
| 4 | Resources | `Icons.Rounded.LibraryBooks` |
| 5 | More | `Icons.Rounded.MoreHoriz` |

- "More" opens a bottom sheet with all available modules.
- Active item uses `primary` color; inactive uses `onSurface` at 60% alpha.

---

## 7. Responsive Breakpoints

| Breakpoint | Width | Layout | Shell |
|------------|-------|--------|-------|
| `Compact` | < 600 dp | Single pane, bottom nav | Mobile |
| `Medium` | 600–839 dp | Tablet sidebar + content | Mobile |
| `Expanded` | 840–1199 dp | 2-pane split | Desktop |
| `Large` | ≥ 1200 dp | Multi-pane workspace | Desktop |

### 7.1 Layout Adaptation

```kotlin
@Composable
fun AdaptiveShell(component: RootComponent) {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> MobileShell(component)
            maxWidth < 840.dp -> TabletShell(component)
            else -> DesktopShell(component)
        }
    }
}
```

---

## 8. Mobile Adaptation

### 8.1 Workspace Drawer

On mobile, the multi-pane workspace collapses to a **single visible pane** with a drawer for switching:

- Swipe from left edge → opens the module drawer via `ModalNavigationDrawer`.
- Module drawer shows all open panes with their category icons.
- Tapping a pane switches the visible content.
- Long-press to close a pane.

### 8.2 Gestures

| Gesture | Action |
|---------|--------|
| Swipe left/right on Bible text | Navigate chapter prev/next |
| Swipe from left edge | Open module drawer |
| Long-press verse | Open context menu |
| Pinch zoom | Adjust Bible text size |
| Pull down | Refresh/reload current module |

### 8.3 Tablet Mode

On tablets (600–839 dp), a persistent sidebar (240 dp) shows the module list, with the selected module's content filling the remaining space. Two-pane split is available in landscape orientation.

---

## 9. Iconography

### 9.1 Icon Set

BibleStudio uses **Material Symbols Rounded** from Compose Material 3 (`Icons.Rounded.*`). Custom icons are provided as vector drawables and Compose `ImageVector`.

### 9.2 Custom Icons

Where Material Symbols lacks domain-specific icons, custom Compose vector icons are used:

| Icon | Usage | Source |
|------|-------|--------|
| `BsIcons.Interlinear` | Morphology/interlinear view | Custom `ImageVector` |
| `BsIcons.Strongs` | Strong's number indicator | Custom `ImageVector` |
| `BsIcons.CrossRef` | Cross-reference link | Custom `ImageVector` |
| `BsIcons.Parallel` | Parallel passage | Custom `ImageVector` |
| `BsIcons.Hebrew` | Hebrew text indicator | Custom `ImageVector` |
| `BsIcons.Greek` | Greek text indicator | Custom `ImageVector` |

Custom icons live in `shared/src/commonMain/kotlin/org/biblestudio/core/icons/` as `ImageVector` builders, eliminating the need for SVG file loading.

### 9.3 Icon Sizing

| Context | Size | Optical Size |
|---------|------|-------------|
| Activity bar | 24 dp | 24 |
| Bottom nav | 24 dp | 24 |
| Pane header | 20 dp | 20 |
| Inline text | 16 dp | 16 |
| FAB | 28 dp | 28 |

---

## 10. Spacing & Layout Grid

### 10.1 Spacing Scale

Based on a 4 dp base unit:

| Token | Value | Usage |
|-------|-------|-------|
| `Space2` | 2 dp | Tight inline spacing |
| `Space4` | 4 dp | Icon-to-text gap |
| `Space8` | 8 dp | Compact padding |
| `Space12` | 12 dp | Card internal padding |
| `Space16` | 16 dp | Standard section padding |
| `Space24` | 24 dp | Between sections |
| `Space32` | 32 dp | Major layout gaps |
| `Space48` | 48 dp | Page-level margins |

```kotlin
object Spacing {
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space48 = 48.dp
}
```

### 10.2 Grid

- Desktop content areas use a **12-column grid** with 16 dp gutters.
- Mobile uses **4-column grid** with 16 dp margins.
- Tablet uses **8-column grid** with 16 dp gutters.

---

## 11. Internationalization (i18n)

### 11.1 Supported Languages

| Language | Code | Status |
|----------|------|--------|
| English | `en` | Primary |
| Spanish | `es` | Supported |

### 11.2 Implementation

BibleStudio uses Compose Multiplatform resources for localization (`composeResources`):

```
shared/src/commonMain/composeResources/
├── values/
│   └── strings.xml              # English (default)
├── values-es/
│   └── strings.xml              # Spanish
```

```xml
<!-- values/strings.xml -->
<resources>
    <string name="chapter_title">%1$s Chapter %2$d</string>
    <string name="search_hint">Search the Bible…</string>
    <plurals name="results_count">
        <item quantity="one">%d result</item>
        <item quantity="other">%d results</item>
    </plurals>
</resources>
```

```kotlin
// Usage in composable
Text(stringResource(Res.string.chapter_title, bookName, chapter))
```

### 11.3 Rules

- All user-facing strings are localized. No hard-coded UI text.
- Plurals use Android-style plural resources.
- Bible text language is independent of the UI language — a Spanish-speaking user can read an English Bible.
- Right-to-left (RTL) layout support is included for future Hebrew/Arabic UI localization via `LayoutDirection`.

---

## 12. Accessibility

### 12.1 Contrast

- All text meets **WCAG 2.1 AA** contrast ratios:
  - Normal text: ≥ 4.5:1
  - Large text (≥ 18 sp or ≥ 14 sp bold): ≥ 3:1
- Highlight backgrounds are tested against `onSurface` text in both themes.
- Focus indicators use a 2 dp `primary` ring with ≥ 3:1 contrast against adjacent colors.

### 12.2 Screen Reader

- Every interactive composable uses `Modifier.semantics {}` with appropriate labels.
- Verse numbers include semantic labels: `"Verse 3"` not just `"3"`.
- Pane headers announce: `"Bible Reader pane, Genesis chapter 1"`.
- Decorative elements use `Modifier.clearAndSetSemantics {}` to hide them.
- Images include `contentDescription` parameters.

```kotlin
Icon(
    imageVector = BsIcons.CrossRef,
    contentDescription = "Cross reference",
    modifier = Modifier.semantics { role = Role.Button },
)
```

### 12.3 Keyboard Navigation

| Key | Action |
|-----|--------|
| `Tab` / `Shift+Tab` | Move focus between panes |
| `Arrow Up/Down` | Navigate verses within a pane |
| `Enter` | Select focused verse / activate button |
| `Escape` | Close dialog / deselect |
| `Ctrl+F` | Focus search |
| `Ctrl+1` through `Ctrl+9` | Switch to pane by position |
| `Ctrl+N` | New note on selected verse |
| `Ctrl+B` | Toggle bookmark on selected verse |

Keyboard shortcuts are registered via `Modifier.onKeyEvent {}` and `Modifier.onPreviewKeyEvent {}`.

### 12.4 Reduced Motion

- All animations respect the platform reduced-motion setting (see §5.4).
- When reduced motion is enabled, transitions are instant (0 ms duration).
- Parallax and decorative animations are fully suppressed.

### 12.5 Text Scaling

- Bible text uses the independent text scale slider (14–28 sp).
- UI text respects the system text scale up to 2.0×.
- Layouts are tested at 200% text scale to ensure no overflow or clipping.

---

## 13. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, layers, DI |
| [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) | Per-platform UI differences, adaptive shells |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | Composable conventions, file naming |
| [MODULE_SYSTEM.md](MODULE_SYSTEM.md) | PaneContainer integration, module catalog |
