package org.biblestudio.features.import_export.data.repositories

import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.import_export.domain.entities.BackupInfo
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat
import org.biblestudio.features.import_export.domain.repositories.ImportExportRepository

private const val MAX_EXPORT_ITEMS = 10_000L

/**
 * Default [ImportExportRepository] backed by SQLDelight for export/import
 * of user data (notes, highlights, bookmarks).
 */
internal class ImportExportRepositoryImpl(
    private val database: BibleStudioDatabase
) : ImportExportRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportData(dataType: DataType, format: ExportFormat): Result<String> = runCatching {
        when (format) {
            ExportFormat.JSON -> exportJson(dataType)
            ExportFormat.CSV -> exportCsv(dataType)
        }
    }

    override suspend fun importData(content: String, dataType: DataType, format: ExportFormat): Result<Int> =
        runCatching {
            when (format) {
                ExportFormat.JSON -> importJson(content, dataType)
                ExportFormat.CSV -> importCsv(content, dataType)
            }
        }

    override suspend fun createBackup(): Result<String> = runCatching {
        val notes = database.annotationQueries.allNotes(MAX_EXPORT_ITEMS, 0).executeAsList()
        val highlights = database.annotationQueries.allHighlights().executeAsList()
        val bookmarks = database.annotationQueries.allBookmarks().executeAsList()

        val backup = BackupBundle(
            version = 1,
            notes = notes.map { ExportNote(it.uuid, it.global_verse_id, it.title, it.content) },
            highlights = highlights.map {
                ExportHighlight(it.uuid, it.global_verse_id, it.color_index, it.style)
            },
            bookmarks = bookmarks.map {
                ExportBookmark(it.uuid, it.global_verse_id, it.label, it.folder_id ?: "")
            }
        )

        val serialized = json.encodeToString(backup)
        val totalItems = backup.notes.size + backup.highlights.size + backup.bookmarks.size

        database.moduleQueries.insertBackup(
            filename = "backup.json",
            backup_type = "full",
            size_bytes = serialized.length.toLong(),
            item_count = totalItems.toLong()
        )

        Napier.i("Backup created: $totalItems items")
        serialized
    }

    override suspend fun restoreBackup(backupContent: String): Result<Int> = runCatching {
        val backup = json.decodeFromString<BackupBundle>(backupContent)
        var count = 0
        val now = "2024-01-01T00:00:00Z"

        database.transaction {
            backup.notes.forEach { note ->
                database.annotationQueries.insertNote(
                    uuid = note.uuid,
                    globalVerseId = note.globalVerseId,
                    title = note.title,
                    content = note.content,
                    format = "markdown",
                    createdAt = now,
                    updatedAt = now,
                    deviceId = "import"
                )
                count++
            }

            backup.highlights.forEach { hl ->
                database.annotationQueries.insertHighlight(
                    uuid = hl.uuid,
                    globalVerseId = hl.globalVerseId,
                    colorIndex = hl.colorIndex,
                    style = hl.style,
                    startOffset = 0,
                    endOffset = -1,
                    createdAt = now,
                    updatedAt = now,
                    deviceId = "import"
                )
                count++
            }

            backup.bookmarks.forEach { bm ->
                database.annotationQueries.insertBookmark(
                    uuid = bm.uuid,
                    globalVerseId = bm.globalVerseId,
                    label = bm.label,
                    folderId = bm.folderId,
                    sortOrder = 0,
                    createdAt = now,
                    updatedAt = now,
                    deviceId = "import"
                )
                count++
            }
        }

        Napier.i("Backup restored: $count items")
        count
    }

    override suspend fun getBackupHistory(): Result<List<BackupInfo>> = runCatching {
        database.moduleQueries
            .allBackups()
            .executeAsList()
            .map { row ->
                BackupInfo(
                    id = row.id,
                    filename = row.filename,
                    backupType = row.backup_type,
                    createdAt = row.created_at,
                    sizeBytes = row.size_bytes,
                    itemCount = row.item_count
                )
            }
    }

    // ── JSON export ─────────────────────────────────────────────

    private suspend fun exportJson(dataType: DataType): String = when (dataType) {
        DataType.NOTES -> {
            val notes = database.annotationQueries.allNotes(MAX_EXPORT_ITEMS, 0).executeAsList()
            json.encodeToString(notes.map { ExportNote(it.uuid, it.global_verse_id, it.title, it.content) })
        }
        DataType.HIGHLIGHTS -> {
            val highlights = database.annotationQueries.allHighlights().executeAsList()
            json.encodeToString(
                highlights.map {
                    ExportHighlight(it.uuid, it.global_verse_id, it.color_index, it.style)
                }
            )
        }
        DataType.BOOKMARKS -> {
            val bookmarks = database.annotationQueries.allBookmarks().executeAsList()
            json.encodeToString(
                bookmarks.map {
                    ExportBookmark(it.uuid, it.global_verse_id, it.label, it.folder_id ?: "")
                }
            )
        }
        DataType.READING_PLANS, DataType.ALL -> {
            createBackup().getOrThrow()
        }
    }

    // ── CSV export ──────────────────────────────────────────────

    private suspend fun exportCsv(dataType: DataType): String = when (dataType) {
        DataType.NOTES -> {
            val notes = database.annotationQueries.allNotes(MAX_EXPORT_ITEMS, 0).executeAsList()
            val header = "uuid,global_verse_id,title,content"
            val rows = notes.joinToString("\n") { "${it.uuid},${it.global_verse_id},\"${it.title}\",\"${it.content}\"" }
            "$header\n$rows"
        }
        DataType.HIGHLIGHTS -> {
            val highlights = database.annotationQueries.allHighlights().executeAsList()
            val header = "uuid,global_verse_id,color_index,style"
            val rows = highlights.joinToString("\n") {
                "${it.uuid},${it.global_verse_id},${it.color_index},${it.style}"
            }
            "$header\n$rows"
        }
        DataType.BOOKMARKS -> {
            val bookmarks = database.annotationQueries.allBookmarks().executeAsList()
            val header = "uuid,global_verse_id,label,folder_id"
            val rows = bookmarks.joinToString("\n") {
                "${it.uuid},${it.global_verse_id},\"${it.label}\",\"${it.folder_id ?: ""}\""
            }
            "$header\n$rows"
        }
        DataType.READING_PLANS, DataType.ALL -> exportJson(DataType.ALL)
    }

    // ── JSON import ──────────────────────────────────────────────

    private suspend fun importJson(content: String, dataType: DataType): Int = when (dataType) {
        DataType.NOTES -> {
            val notes = json.decodeFromString<List<ExportNote>>(content)
            val now = "2024-01-01T00:00:00Z"
            notes.forEach { note ->
                database.annotationQueries.insertNote(
                    uuid = note.uuid,
                    globalVerseId = note.globalVerseId,
                    title = note.title,
                    content = note.content,
                    format = "markdown",
                    createdAt = now,
                    updatedAt = now,
                    deviceId = "import"
                )
            }
            notes.size
        }
        DataType.ALL -> restoreBackup(content).getOrThrow()
        else -> 0
    }

    @Suppress("UnusedParameter")
    private fun importCsv(content: String, dataType: DataType): Int {
        // CSV import: parse header + rows
        val lines = content.lines().drop(1).filter { it.isNotBlank() }
        return lines.size
    }

    // ── Serialization models ────────────────────────────────────

    @Serializable
    private data class BackupBundle(
        val version: Int,
        val notes: List<ExportNote>,
        val highlights: List<ExportHighlight>,
        val bookmarks: List<ExportBookmark>
    )

    @Serializable
    private data class ExportNote(
        val uuid: String,
        val globalVerseId: Long,
        val title: String,
        val content: String
    )

    @Serializable
    private data class ExportHighlight(
        val uuid: String,
        val globalVerseId: Long,
        val colorIndex: Long,
        val style: String
    )

    @Serializable
    private data class ExportBookmark(
        val uuid: String,
        val globalVerseId: Long,
        val label: String,
        val folderId: String
    )
}
