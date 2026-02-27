package org.biblestudio.features.search.domain.syntax

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for SyntaxLexer, SyntaxParser, and SyntaxQueryBuilder.
 */
class SyntaxSearchTest {

    // ──────────────────────────────── Lexer ────────────────────────────────

    @Test
    fun `lexer tokenizes LEMMA expression`() {
        val tokens = SyntaxLexer.tokenize("[LEMMA:H1234]")
        assertEquals(TokenType.LBRACKET, tokens[0].type)
        assertEquals(TokenType.KEYWORD, tokens[1].type)
        assertEquals("LEMMA", tokens[1].value)
        assertEquals(TokenType.COLON, tokens[2].type)
        assertEquals(TokenType.VALUE, tokens[3].type)
        assertEquals("H1234", tokens[3].value)
        assertEquals(TokenType.RBRACKET, tokens[4].type)
        assertEquals(TokenType.EOF, tokens[5].type)
    }

    @Test
    fun `lexer tokenizes WITHIN expression`() {
        val tokens = SyntaxLexer.tokenize("[WITHIN 3 WORDS]")
        assertEquals(TokenType.LBRACKET, tokens[0].type)
        assertEquals(TokenType.KEYWORD, tokens[1].type)
        assertEquals("WITHIN", tokens[1].value)
        assertEquals(TokenType.NUMBER, tokens[2].type)
        assertEquals("3", tokens[2].value)
        assertEquals(TokenType.WORDS, tokens[3].type)
        assertEquals(TokenType.RBRACKET, tokens[4].type)
    }

    @Test
    fun `lexer tokenizes plain text as VALUE`() {
        val tokens = SyntaxLexer.tokenize("love")
        assertEquals(TokenType.VALUE, tokens[0].type)
        assertEquals("love", tokens[0].value)
        assertEquals(TokenType.EOF, tokens[1].type)
    }

    // ──────────────────────────────── Parser ────────────────────────────────

    @Test
    fun `parser creates LemmaNode from LEMMA expression`() {
        val node = SyntaxParser.parseQuery("[LEMMA:H7225]")
        assertIs<SyntaxNode.LemmaNode>(node)
        assertEquals("H7225", node.strongsNumber)
    }

    @Test
    fun `parser creates PosNode from POS expression`() {
        val node = SyntaxParser.parseQuery("[POS:Noun]")
        assertIs<SyntaxNode.PosNode>(node)
        assertEquals("Noun", node.partOfSpeech)
    }

    @Test
    fun `parser creates TextNode from plain text`() {
        val node = SyntaxParser.parseQuery("love")
        assertIs<SyntaxNode.TextNode>(node)
        assertEquals("love", node.text)
    }

    @Test
    fun `parser creates AndNode for multiple clauses`() {
        val node = SyntaxParser.parseQuery("[LEMMA:H7225] [POS:Noun]")
        assertIs<SyntaxNode.AndNode>(node)
        assertEquals(2, node.children.size)
        assertIs<SyntaxNode.LemmaNode>(node.children[0])
        assertIs<SyntaxNode.PosNode>(node.children[1])
    }

    @Test
    fun `parser creates ProximityNode from WITHIN clause`() {
        val node = SyntaxParser.parseQuery("[LEMMA:H7225] [LEMMA:H430] [WITHIN 3 WORDS]")
        assertIs<SyntaxNode.ProximityNode>(node)
        assertEquals(3, node.distance)
        assertIs<SyntaxNode.LemmaNode>(node.left)
        assertIs<SyntaxNode.LemmaNode>(node.right)
    }

    @Test
    fun `parser returns empty TextNode for empty input`() {
        val node = SyntaxParser.parseQuery("")
        assertIs<SyntaxNode.TextNode>(node)
        assertEquals("", node.text)
    }

    // ──────────────────────────────── QueryBuilder ────────────────────────────────

    @Test
    fun `buildQuery generates SQL for LemmaNode`() {
        val (sql, params) = SyntaxQueryBuilder.buildQuery(SyntaxNode.LemmaNode("H7225"))
        assertTrue(sql.contains("word_occurrences"))
        assertTrue(sql.contains("strongs_number = ?"))
        assertEquals(listOf("H7225"), params)
    }

    @Test
    fun `buildQuery generates SQL for PosNode`() {
        val (sql, params) = SyntaxQueryBuilder.buildQuery(SyntaxNode.PosNode("Noun"))
        assertTrue(sql.contains("morphology"))
        assertTrue(sql.contains("pos = ?"))
        assertEquals(listOf("Noun"), params)
    }

    @Test
    fun `buildQuery generates SQL for TextNode`() {
        val (sql, params) = SyntaxQueryBuilder.buildQuery(SyntaxNode.TextNode("love"))
        assertTrue(sql.contains("fts_verses"))
        assertTrue(sql.contains("MATCH ?"))
        assertEquals(listOf("love"), params)
    }

    @Test
    fun `buildQuery combines AndNode children with AND`() {
        val node = SyntaxNode.AndNode(
            listOf(
                SyntaxNode.LemmaNode("H7225"),
                SyntaxNode.PosNode("Noun")
            )
        )
        val (sql, params) = SyntaxQueryBuilder.buildQuery(node)
        assertTrue(sql.contains("AND"))
        assertEquals(2, params.size)
    }
}
