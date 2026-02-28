package org.biblestudio.features.passage_guide.data.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository
import org.biblestudio.features.passage_guide.data.mappers.toOutline
import org.biblestudio.features.passage_guide.domain.entities.PassageReport
import org.biblestudio.features.passage_guide.domain.repositories.PassageGuideRepository
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

internal class PassageGuideRepositoryImpl(
    private val database: BibleStudioDatabase,
    private val bibleRepository: BibleRepository,
    private val crossRefRepository: CrossRefRepository,
    private val morphologyRepository: MorphologyRepository,
    private val wordStudyRepository: WordStudyRepository,
    private val resourceRepository: ResourceRepository,
    private val noteRepository: NoteRepository
) : PassageGuideRepository {

    @Suppress("LongMethod")
    override suspend fun buildReport(globalVerseId: Long): Result<PassageReport> = runCatching {
        coroutineScope {
            val verseDeferred = async {
                bibleRepository.getVerseByGlobalId(globalVerseId)
            }
            val crossRefsDeferred = async {
                crossRefRepository.getAllForVerse(globalVerseId)
            }
            val outlinesDeferred = async {
                runCatching {
                    database.studyQueries
                        .outlinesForVerse(globalVerseId)
                        .executeAsList()
                        .map { it.toOutline() }
                }
            }
            val morphDeferred = async {
                morphologyRepository.getMorphWords(globalVerseId)
            }
            val commentaryDeferred = async {
                val resources = resourceRepository.getByType("commentary")
                    .getOrDefault(emptyList())
                runCatching {
                    resources.flatMap { resource ->
                        resourceRepository.getEntriesForVerse(resource.uuid, globalVerseId)
                            .getOrDefault(emptyList())
                    }
                }
            }
            val notesDeferred = async {
                noteRepository.getNotesForVerse(globalVerseId)
            }

            val verseText = verseDeferred.await()
                .getOrNull()?.text ?: ""
            val crossRefs = crossRefsDeferred.await()
                .getOrDefault(emptyList())
            val outlines = outlinesDeferred.await()
                .getOrDefault(emptyList())
            val morphWords = morphDeferred.await()
                .getOrDefault(emptyList())
            val commentary = commentaryDeferred.await()
                .getOrDefault(emptyList())
            val notes = notesDeferred.await()
                .getOrDefault(emptyList())

            // Extract key words: unique Strong's numbers from morphology → lexicon lookup
            val uniqueStrongs = morphWords.map { it.strongsNumber }.distinct()
            val keyWords = uniqueStrongs.mapNotNull { strongs ->
                wordStudyRepository.lookupByStrongs(strongs).getOrNull()
            }

            PassageReport(
                verseId = globalVerseId,
                verseText = verseText,
                crossReferences = crossRefs,
                outlines = outlines,
                keyWords = keyWords,
                commentaryEntries = commentary,
                userNotes = notes,
                morphologyWords = morphWords
            )
        }
    }
}
