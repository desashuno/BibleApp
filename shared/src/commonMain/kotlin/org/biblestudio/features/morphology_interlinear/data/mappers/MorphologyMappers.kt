package org.biblestudio.features.morphology_interlinear.data.mappers

import migrations.Alignment_words
import migrations.Word_occurrences
import org.biblestudio.database.MorphologyByStrongs
import org.biblestudio.database.MorphologyForVerse
import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphWord
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.features.morphology_interlinear.domain.entities.WordOccurrence

internal fun MorphologyForVerse.toMorphologyData(): MorphologyData = MorphologyData(
    id = id,
    globalVerseId = global_verse_id,
    wordPosition = word_position,
    strongsNumber = strongs_number,
    parsingCode = parsing_code,
    originalWord = original_word,
    transliteration = transliteration,
    definition = definition
)

internal fun MorphologyForVerse.toMorphWord(): MorphWord = MorphWord(
    id = id,
    globalVerseId = global_verse_id,
    wordPosition = word_position,
    surfaceForm = surface_form,
    lemma = lemma,
    strongsNumber = strongs_number,
    parsingCode = parsing_code,
    gloss = gloss
)

internal fun MorphologyByStrongs.toMorphWord(): MorphWord = MorphWord(
    id = id,
    globalVerseId = global_verse_id,
    wordPosition = word_position,
    surfaceForm = surface_form,
    lemma = lemma,
    strongsNumber = strongs_number,
    parsingCode = parsing_code,
    gloss = gloss
)

internal fun Word_occurrences.toWordOccurrence(): WordOccurrence = WordOccurrence(
    id = id,
    strongsNumber = strongs_number,
    globalVerseId = global_verse_id,
    wordPosition = word_position
)

internal fun Alignment_words.toAlignmentEntry(): AlignmentEntry = AlignmentEntry(
    id = id,
    globalVerseId = global_verse_id,
    englishPosition = english_position,
    englishToken = english_token,
    originalPosition = original_position,
    strongsNumber = strongs_number
)
