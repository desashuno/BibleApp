# Security

> BibleStudio — Threat Model, Data Protection, Input Validation & Permissions (Kotlin Multiplatform)

---

## 1. Threat Model

### 1.1 Scope

BibleStudio is a **local-first** application. There is no remote API for core functionality. The primary attack surface is:

| Vector | Risk Level | Description |
|--------|-----------|-------------|
| **Module import** | Medium | User imports `.bsmodule` files from untrusted sources |
| **File export** | Low | User exports data to local filesystem |
| **Clipboard** | Low | Copy/paste of verse text, notes |
| **Local database** | Low | SQLite file on device storage |
| **Future sync** | Medium | Device-to-device data transfer (when implemented) |

### 1.2 Non-Threats

- **Network attacks** — no remote endpoints in current version.
- **Authentication bypass** — no user accounts or authentication.
- **API abuse** — no public API.

---

## 2. Data Protection

### 2.1 At Rest

| Platform | Storage Location | Encryption | Access Control |
|----------|-----------------|------------|----------------|
| Android | App-private `/data/data/` | Android file-based encryption | App sandbox |
| iOS | App container (Documents) | iOS Data Protection (Complete) | App sandbox |
| Windows | `%APPDATA%\BibleStudio\` | User-level NTFS permissions | User session |
| macOS | `~/Library/Application Support/` | FileVault (if enabled) | App Sandbox |
| Linux | `~/.local/share/biblestudio/` | User-level permissions | File permissions |

- The SQLite database is **not independently encrypted** — it relies on platform-level encryption.
- If independent encryption is required in a future version, `sqlcipher` can replace the default SQLite driver via a custom `SqlDriver` implementation.

### 2.2 In Transit

- Current version: **No network traffic** for core features.
- Future sync: All device-to-device transfers will use **TLS 1.3** minimum (via `ktor-client` or platform sockets).
- External links: Opened in the system browser via platform `expect`/`actual` URL launcher, which handles its own TLS.

### 2.3 Backup Considerations

- iOS: Database is included in iCloud/iTunes backups (protected by Apple's backup encryption).
- Android: Database is excluded from Auto Backup by default via `android:allowBackup="false"` in the manifest. Users can export data manually.
- Desktop: Users are responsible for backing up the database file.

---

## 3. User Data Inventory

| Data Type | Content | Sensitivity | Sync-Eligible |
|-----------|---------|-------------|--------------|
| Bible text | Public domain / licensed text | Low | No (bundled) |
| Notes | User-written study notes | **High** | Yes |
| Highlights | Verse-level color markers | Medium | Yes |
| Bookmarks | Labeled verse references | Medium | Yes |
| Sermons | User-written sermon content | **High** | Yes |
| Search history | Recent search queries | Medium | Yes |
| Settings | App preferences | Low | Yes |
| Workspace layouts | Pane arrangement JSON | Low | Yes |

### Data Retention

- User data is retained indefinitely unless the user explicitly deletes it.
- Soft-deleted data (see [DATA_LAYER.md](DATA_LAYER.md) §7.3) is purged after 90 days.
- Uninstalling the app removes all data (platform-default behavior).

---

## 4. Import Validation

Module imports (`.bsmodule` files) are the primary vector for untrusted data. Every import follows a strict validation pipeline:

### 4.1 File Structure Validation

```kotlin
suspend fun importModule(file: PlatformFile): ImportResult {
    // 1. Check file extension
    if (!file.name.endsWith(".bsmodule")) {
        return ImportResult.Rejected("Invalid file type")
    }

    // 2. Check file size (max 500 MB)
    if (file.size > 500 * 1024 * 1024) {
        return ImportResult.Rejected("File exceeds 500 MB limit")
    }

    // 3. Parse as ZIP archive
    val archive = ZipInputStream(file.inputStream())

    // 4. Validate manifest
    val manifest = archive.findEntry("manifest.json")
        ?: return ImportResult.Rejected("Missing manifest.json")

    // 5. Validate manifest schema
    val manifestData = Json.decodeFromString<ModuleManifest>(manifest.readText())
    if (!validateManifestSchema(manifestData)) {
        return ImportResult.Rejected("Invalid manifest format")
    }

    // 6. Process content files with sanitization
    return processModuleContent(archive, manifestData)
}
```

### 4.2 Content Sanitization

| Content Type | Validation |
|-------------|-----------|
| Bible text (plain) | Strip HTML tags, normalize whitespace |
| Bible text (HTML) | Allowlist: `<b>`, `<i>`, `<em>`, `<strong>`, `<br>`, `<sup>`. Strip all others |
| Resource text | Same HTML allowlist + `<h1>`–`<h6>`, `<p>`, `<ul>`, `<ol>`, `<li>` |
| JSON metadata | Schema validation via `kotlinx.serialization` against expected `@Serializable` classes |
| File paths | Reject paths containing `..`, absolute paths, or non-ASCII control characters |

---

## 5. SQL Safety

### 5.1 SQLDelight Parameterization

SQLDelight generates **parameterized queries** at compile time from `.sq` files. All user input flows through bound parameters, which prevents SQL injection:

```sql
-- Bible.sq — parameters are bound, never interpolated
searchVerses:
SELECT v.* FROM fts_verses fv
JOIN verses v ON v.id = fv.rowid
WHERE fts_verses MATCH :query;
```

```kotlin
// Generated code uses PreparedStatement binding — safe by default
database.bibleQueries.searchVerses(query = userInput).executeAsList()
```

### 5.2 Prohibited Patterns

```kotlin
// NEVER do this — string interpolation in SQL
driver.execute(null, "SELECT * FROM verses WHERE text LIKE '%$userInput%'", 0)

// NEVER do this — concatenation in SQL
driver.execute(null, "DROP TABLE " + tableName, 0)
```

### 5.3 Rules

- **All queries are defined in `.sq` files** — SQLDelight generates type-safe Kotlin at compile time.
- **`driver.execute()` with raw SQL is only used in migrations** (`.sqm` files) — never with user-provided values.
- **No raw SQLite access** outside of SQLDelight-generated code.
- Code review must flag any use of string interpolation or concatenation near SQL.

---

## 6. Input Sanitization

### 6.1 User-Generated Content

| Field | Max Length | Allowed Characters | Sanitization |
|-------|-----------|-------------------|-------------|
| Note title | 200 | Unicode text | Trim whitespace, strip control chars |
| Note content | 50,000 | Unicode text + limited HTML | HTML allowlist |
| Bookmark label | 100 | Unicode text | Trim whitespace |
| Sermon title | 200 | Unicode text | Trim whitespace, strip control chars |
| Search query | 500 | Unicode text | Escape FTS5 special chars: `*`, `"`, `(`, `)` |
| Workspace name | 50 | Unicode text | Trim whitespace |

### 6.2 HTML Sanitization

```kotlin
fun sanitizeHtml(input: String): String {
    val allowedPattern = Regex("""</?(?:b|i|em|strong|br|sup|h[1-6]|p|ul|ol|li)(?:\s*/?)>""")
    return input.replace(Regex("<[^>]+>")) { match ->
        if (allowedPattern.matches(match.value)) match.value else ""
    }
}
```

---

## 7. Platform Permissions

### 7.1 Principle of Least Privilege

BibleStudio requests **only the permissions necessary** for its core functionality:

### 7.2 Android

```xml
<!-- AndroidManifest.xml -->
<!-- No permissions required for core functionality -->
<!-- File picker uses Storage Access Framework (no permission needed) -->
<!-- Audio playback uses media APIs (no permission needed) -->

<!-- Optional: Notifications (reading plan reminders) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 7.3 iOS

```xml
<!-- Info.plist -->
<!-- No special permissions for core functionality -->
<!-- File picker uses UIDocumentPickerViewController -->
<!-- Audio uses AVAudioSession (no permission prompt) -->
```

### 7.4 macOS Entitlements

```xml
<!-- Release.entitlements -->
<key>com.apple.security.app-sandbox</key>
<true/>
<key>com.apple.security.files.user-selected.read-write</key>
<true/>
<key>com.apple.security.network.client</key>
<true/>
```

- **App Sandbox**: Enabled (required for App Store).
- **User-selected files**: Read/write access to files chosen via file picker.
- **Network client**: Required for future sync features.

### 7.5 Windows & Linux

No special permissions required. File access is unrestricted within user space. Desktop apps run on JVM with standard user privileges.

---

## 8. Dependency Security

### 8.1 Audit Schedule

- Run `./gradlew dependencyUpdates` monthly (via Gradle Versions Plugin).
- Review dependency changelogs before upgrading.
- Pin dependency versions in `libs.versions.toml` (Gradle version catalog).

### 8.2 Supply Chain

- All dependencies are sourced from **Maven Central** and **Google's Maven repository**.
- Gradle dependency verification (`gradle/verification-metadata.xml`) can be enabled to verify checksums.
- `gradle.lockfile` is committed to version control when dependency locking is enabled.

### 8.3 Critical Dependencies

| Library | Role | Risk if Compromised |
|---------|------|-------------------|
| `SQLDelight` | Database code generation & drivers | Full data access |
| `sqlite-jdbc` | JDBC SQLite driver (desktop) | Code execution |
| `Koin` | DI container | Service substitution |
| `Decompose` | Navigation & lifecycle | State manipulation |
| `Compose Multiplatform` | UI framework | UI rendering manipulation |

These libraries are maintained by well-known Kotlin/JetBrains community members, published on Maven Central with PGP signatures. Monitor their repositories for security advisories.

---

## 9. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Error handling, logging |
| [DATA_LAYER.md](DATA_LAYER.md) | Database schema, sync, soft deletes |
| [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) | Platform-specific storage, file access |
| [MODULE_SYSTEM.md](MODULE_SYSTEM.md) | Module import format |
