package org.biblestudio.core.data_manager

import migrations.Data_modules
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.database.BibleStudioDatabase

/**
 * SQLDelight-backed [DataModuleRepository].
 */
internal class DataModuleRepositoryImpl(
    private val database: BibleStudioDatabase
) : DataModuleRepository {

    override suspend fun getAllModules(): Result<List<DataModuleDescriptor>> = runCatching {
        database.dataModuleQueries.allModules().executeAsList().map { it.toDescriptor() }
    }

    override suspend fun getModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>> = runCatching {
        database.dataModuleQueries.modulesByType(type.value).executeAsList().map { it.toDescriptor() }
    }

    override suspend fun getModulesByStatus(status: DataModuleStatus): Result<List<DataModuleDescriptor>> =
        runCatching {
            database.dataModuleQueries.modulesByStatus(status.value).executeAsList().map { it.toDescriptor() }
        }

    override suspend fun getModuleById(moduleId: String): Result<DataModuleDescriptor?> = runCatching {
        database.dataModuleQueries.moduleById(moduleId).executeAsOneOrNull()?.toDescriptor()
    }

    override suspend fun insertModule(descriptor: DataModuleDescriptor): Result<Unit> = runCatching {
        database.dataModuleQueries.insertModule(
            moduleId = descriptor.moduleId,
            name = descriptor.name,
            description = descriptor.description,
            type = descriptor.type.value,
            version = descriptor.version,
            language = descriptor.language,
            sourceUrl = descriptor.sourceUrl,
            sizeBytes = descriptor.sizeBytes,
            status = descriptor.status.value,
            checksum = descriptor.checksum,
            metadata = descriptor.metadata,
            isActive = if (descriptor.isActive) 1L else 0L
        )
    }

    override suspend fun updateStatus(moduleId: String, status: DataModuleStatus): Result<Unit> = runCatching {
        database.dataModuleQueries.updateStatus(status = status.value, moduleId = moduleId)
    }

    override suspend fun updateProgress(moduleId: String, progress: Float): Result<Unit> = runCatching {
        database.dataModuleQueries.updateProgress(progress = progress.toDouble(), moduleId = moduleId)
    }

    override suspend fun markInstalled(moduleId: String): Result<Unit> = runCatching {
        database.dataModuleQueries.markInstalled(moduleId)
    }

    override suspend fun markRemoved(moduleId: String): Result<Unit> = runCatching {
        database.dataModuleQueries.markRemoved(moduleId)
    }

    override suspend fun deleteModule(moduleId: String): Result<Unit> = runCatching {
        database.dataModuleQueries.deleteModule(moduleId)
    }

    override suspend fun getActiveModules(): Result<List<DataModuleDescriptor>> = runCatching {
        database.dataModuleQueries.activeModules().executeAsList().map { it.toDescriptor() }
    }

    override suspend fun getActiveModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>> =
        runCatching {
            database.dataModuleQueries.activeModulesByType(type.value)
                .executeAsList()
                .map { it.toDescriptor() }
        }

    override suspend fun setModuleActive(moduleId: String, isActive: Boolean): Result<Unit> = runCatching {
        database.dataModuleQueries.setModuleActive(isActive = if (isActive) 1L else 0L, moduleId = moduleId)
    }

    @Suppress("LongMethod", "TooGenericExceptionCaught", "CyclomaticComplexMethod")
    override suspend fun autoPopulateFromExistingData(): Result<Int> = runCatching {
        var count = 0
        val dq = database.dataModuleQueries

        // Default-active Bible abbreviations (lowercase)
        val defaultActiveBibles = setOf("rvr1960", "rv2020", "kjv", "asv")

        // ── Bibles ──
        try {
            val bibles = database.bibleQueries.allBibles().executeAsList()
            for (bible in bibles) {
                val moduleId = "bible-${bible.abbreviation.lowercase()}"
                val isActive = bible.abbreviation.lowercase() in defaultActiveBibles
                dq.insertModule(
                    moduleId = moduleId,
                    name = bible.name,
                    description = "Bible: ${bible.abbreviation}",
                    type = "bible",
                    version = "1.0",
                    language = bible.language,
                    sourceUrl = "",
                    sizeBytes = 0,
                    status = "installed",
                    checksum = "",
                    metadata = "{}",
                    isActive = if (isActive) 1L else 0L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Morphology ──
        try {
            val morphCount = database.studyQueries.morphologyWordCount().executeAsOne()
            if (morphCount > 0) {
                dq.insertModule(
                    "morphology-hebrew-ot", "Hebrew OT Morphology",
                    "Word-by-word Hebrew morphology (STEPBible TAHOT)", "morphology",
                    "1.0", "he", "", 0, "installed", "", "{}", 1L
                )
                dq.insertModule(
                    "morphology-greek-nt", "Greek NT Morphology",
                    "Word-by-word Greek morphology (STEPBible TAGNT)", "morphology",
                    "1.0", "el", "", 0, "installed", "", "{}", 1L
                )
                count += 2
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Lexicon ──
        try {
            val lexCount = database.studyQueries.lexiconCount().executeAsOne()
            if (lexCount > 0) {
                dq.insertModule(
                    "lexicon-hebrew", "Hebrew Lexicon",
                    "Hebrew word definitions (STEPBible TBESH)", "dictionary",
                    "1.0", "he", "", 0, "installed", "", "{}", 1L
                )
                dq.insertModule(
                    "lexicon-greek", "Greek Lexicon",
                    "Greek word definitions (STEPBible TBESG)", "dictionary",
                    "1.0", "el", "", 0, "installed", "", "{}", 1L
                )
                count += 2
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Cross-References ──
        try {
            val xrefCount = database.referenceQueries.crossRefCount().executeAsOne()
            if (xrefCount > 0) {
                dq.insertModule(
                    "cross-references", "Cross-References",
                    "Treasury of Scripture Knowledge cross-references", "cross_references",
                    "1.0", "en", "", 0, "installed", "", "{}", 1L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Geography ──
        try {
            val geoCount = database.atlasQueries.locationCount().executeAsOne()
            if (geoCount > 0) {
                dq.insertModule(
                    "geography", "Bible Geography",
                    "Geographic locations from OpenBible.info", "geography",
                    "1.0", "en", "", 0, "installed", "", "{}", 1L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Entities (Knowledge Graph) ──
        try {
            val entCount = database.knowledgeGraphQueries.nodeCount().executeAsOne()
            if (entCount > 0) {
                dq.insertModule(
                    "entities", "Bible Entities",
                    "People, places, and things (STEPBible TIPNR)", "entities",
                    "1.0", "en", "", 0, "installed", "", "{}", 1L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Timeline ──
        try {
            val tlCount = database.timelineQueries.eventCount().executeAsOne()
            if (tlCount > 0) {
                dq.insertModule(
                    "timeline", "Bible Timeline",
                    "Chronological events of the Bible", "timeline",
                    "1.0", "en", "", 0, "installed", "", "{}", 1L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        // ── Resources (commentaries, dictionaries) ──
        try {
            val resources = database.resourceQueries.allResources().executeAsList()
            for (res in resources) {
                val slug = res.title.lowercase().replace(" ", "-").replace("'", "")
                val modType = when (res.type) {
                    "commentary" -> "commentary"
                    "dictionary" -> "dictionary"
                    else -> continue
                }
                val moduleId = "$modType-$slug"
                dq.insertModule(
                    moduleId, res.title, "${res.type}: ${res.title}",
                    modType, "1.0", "en", "", 0, "installed", "", "{}", 1L
                )
                count++
            }
        } catch (_: Exception) { /* table may not exist */ }

        count
    }
}

private fun Data_modules.toDescriptor(): DataModuleDescriptor = DataModuleDescriptor(
    id = id,
    moduleId = module_id,
    name = name,
    description = description,
    type = DataModuleType.fromString(type),
    version = version,
    language = language,
    sourceUrl = source_url,
    sizeBytes = size_bytes,
    status = DataModuleStatus.fromString(status),
    progress = progress.toFloat(),
    installedAt = installed_at,
    updatedAt = updated_at,
    checksum = checksum,
    metadata = metadata,
    isActive = is_active != 0L
)
