package org.biblestudio.features.bible_reader.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the word-level diff algorithm in [DefaultTextComparisonComponent].
 */
class TextComparisonDiffTest {

    @Test
    fun `wordDiff returns EQUAL for identical texts`() {
        val words = listOf("In", "the", "beginning")
        val diff = DefaultTextComparisonComponent.wordDiff(words, words)
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
        val diff = DefaultTextComparisonComponent.wordDiff(wordsA, wordsB)

        val added = diff.filter { it.type == DiffType.ADDED }
        assertEquals(1, added.size)
        assertEquals("very", added[0].text)
    }

    @Test
    fun `wordDiff detects REMOVED words`() {
        val wordsA = listOf("In", "the", "very", "beginning")
        val wordsB = listOf("In", "the", "beginning")
        val diff = DefaultTextComparisonComponent.wordDiff(wordsA, wordsB)

        val removed = diff.filter { it.type == DiffType.REMOVED }
        assertEquals(1, removed.size)
        assertEquals("very", removed[0].text)
    }

    @Test
    fun `wordDiff handles completely different texts`() {
        val wordsA = listOf("hello", "world")
        val wordsB = listOf("foo", "bar")
        val diff = DefaultTextComparisonComponent.wordDiff(wordsA, wordsB)

        val added = diff.filter { it.type == DiffType.ADDED }
        val removed = diff.filter { it.type == DiffType.REMOVED }
        assertEquals(2, added.size)
        assertEquals(2, removed.size)
    }

    @Test
    fun `wordDiff handles empty first text`() {
        val diff = DefaultTextComparisonComponent.wordDiff(emptyList(), listOf("hello", "world"))
        assertEquals(2, diff.size)
        assertTrue(diff.all { it.type == DiffType.ADDED })
    }

    @Test
    fun `wordDiff handles empty second text`() {
        val diff = DefaultTextComparisonComponent.wordDiff(listOf("hello", "world"), emptyList())
        assertEquals(2, diff.size)
        assertTrue(diff.all { it.type == DiffType.REMOVED })
    }

    @Test
    fun `computeDiff returns empty for fewer than two selected versions`() {
        val versions = mapOf("KJV" to "In the beginning")
        val diff = DefaultTextComparisonComponent.computeDiff(versions, listOf("KJV"))
        assertTrue(diff.isEmpty())
    }

    @Test
    fun `computeDiff produces segments for two versions`() {
        val versions = mapOf(
            "KJV" to "In the beginning God created",
            "NIV" to "In the beginning God made"
        )
        val diff = DefaultTextComparisonComponent.computeDiff(versions, listOf("KJV", "NIV"))
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
