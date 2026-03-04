package org.biblestudio.features.word_study.data.mappers

import migrations.Lexicon_entries
import org.biblestudio.core.study.LexiconEntry

internal fun Lexicon_entries.toLexiconEntry(): LexiconEntry = LexiconEntry(
    strongsNumber = strongs_number,
    originalWord = original_word,
    transliteration = transliteration,
    definition = definition,
    usageNotes = usage_notes,
    glossEs = gloss_es,
)
