package org.biblestudio.features.bible_reader.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.biblestudio.features.bible_reader.domain.entities.VersionVerse

/**
 * Tests for the word-level diff algorithm in [DefaultTextComparisonComponent].
 */
class TextComparisonDiffTest {

    @Test
    fun `wordDiff returns EQUAL for identical texts`() {
        val words = listOf("In", "the", "beginning")
        val diff = wordDiff(words, words)
        assertTrue(diff.all { it.type == DiffType.EQUAL })
        assertEquals(3, diff.size)
        assertEquals("In", diff[0].text)
        assertEquals("the", diff[1].text)
        assertEquals("beginning", diff[2].text)
    }

    @Test
    fun `wordDiff detects ADDED words`() {
        val wordsA = listOf("In", "the", "beginning")
        val wordsB = listOf("In", "the", "very", "beginning")
        val diff = wordDiff(wordsA, wordsB)

        val added = diff.filter { it.type == DiffType.ADDED }
        assertEquals(1, added.size)
        assertEquals("very", added[0].text)
    }

    @Test
    fun `wordDiff detects REMOVED words`() {
        val wordsA = listOf("In", "the", "very", "beginning")
        val wordsB = listOf("In", "the", "beginning")
        val diff = wordDiff(wordsA, wordsB)

        val removed = diff.filter { it.type == DiffType.REMOVED }
        assertEquals(1, removed.size)
        assertEquals("very", removed[0].text)
    }

    @Test
    fun `wordDiff handles completely different texts`() {
        val wordsA = listOf("hello", "world")
        val wordsB = listOf("foo", "bar")
        val diff = wordDiff(wordsA, wordsB)

        val added = diff.filter { it.type == DiffType.ADDED }
        val removed = diff.filter { it.type == DiffType.REMOVED }
        assertEquals(2, added.size)
        assertEquals(2, removed.size)
    }

    @Test
    fun `wordDiff handles empty first text`() {
        val diff = wordDiff(emptyList(), listOf("hello", "world"))
        assertEquals(2, diff.size)
        assertTrue(diff.all { it.type == DiffType.ADDED })
    }

    @Test
    fun `wordDiff handles empty second text`() {
        val diff = wordDiff(listOf("hello", "world"), emptyList())
        assertEquals(2, diff.size)
        assertTrue(diff.all { it.type == DiffType.REMOVED })
    }

    @Test
    fun `computeDiff returns empty for fewer than two selected versions`() {
        val versions = mapOf("KJV" to VersionVerse("In the beginning"))
        val diff = DefaultTextComparisonComponent.computeDiff(versions, listOf("KJV"))
        assertTrue(diff.isEmpty())
    }

    @Test
    fun `computeDiff produces segments for two versions`() {
        val versions = mapOf(
            "KJV" to VersionVerse("In the beginning God created"),
            "WEB" to VersionVerse("In the beginning God made")
        )
        val diff = DefaultTextComparisonComponent.computeDiff(versions, listOf("KJV", "WEB"))
        assertTrue(diff.isNotEmpty())

        // "In", "the", "beginning", "God" should be EQUAL
        val equal = diff.filter { it.type == DiffType.EQUAL }
        assertTrue(equal.size >= 4)

        // "created" should be REMOVED, "made" should be ADDED
        val removed = diff.filter { it.type == DiffType.REMOVED }
        val added = diff.filter { it.type == DiffType.ADDED }
        assertEquals(1, removed.size)
        assertEquals("created", removed[0].text)
        assertEquals(1, added.size)
        assertEquals("made", added[0].text)
    }
}
