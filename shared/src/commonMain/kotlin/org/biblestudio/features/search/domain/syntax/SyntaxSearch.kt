package org.biblestudio.features.search.domain.syntax

/**
 * Token types produced by the syntax search lexer.
 */
enum class TokenType {
    /** Opening bracket `[`. */
    LBRACKET,

    /** Closing bracket `]`. */
    RBRACKET,

    /** Keyword like LEMMA, POS, WITHIN. */
    KEYWORD,

    /** Colon separator `:`. */
    COLON,

    /** A literal value (e.g., "H1234", "Noun"). */
    VALUE,

    /** Numeric literal. */
    NUMBER,

    /** The WORDS keyword in "WITHIN n WORDS". */
    WORDS,

    /** End of input. */
    EOF
}

/**
 * A lexer token.
 */
data class Token(val type: TokenType, val value: String)

/**
 * AST node types for syntax search queries.
 *
 * Examples:
 * - `[LEMMA:H1234]` → [LemmaNode]
 * - `[POS:Noun]` → [PosNode]
 * - `[WITHIN 3 WORDS]` → [ProximityNode]
 * - Multiple clauses are combined with [AndNode]
 */
sealed class SyntaxNode {

    /** Match verses containing a specific lemma (Strong's number). */
    data class LemmaNode(val strongsNumber: String) : SyntaxNode()

    /** Match verses containing a specific part-of-speech tag. */
    data class PosNode(val partOfSpeech: String) : SyntaxNode()

    /** Proximity constraint: two nodes must occur within N words. */
    data class ProximityNode(val left: SyntaxNode, val right: SyntaxNode, val distance: Int) : SyntaxNode()

    /** Conjunction: all child nodes must match. */
    data class AndNode(val children: List<SyntaxNode>) : SyntaxNode()

    /** A plain text search term (falls back to FTS). */
    data class TextNode(val text: String) : SyntaxNode()
}

/**
 * Tokenizes a syntax search query string into a list of [Token]s.
 *
 * Supported syntax:
 * - `[LEMMA:H1234]` — Strong's number lookup
 * - `[POS:Noun]` — Part-of-speech filter
 * - `[WITHIN 3 WORDS]` — Proximity constraint
 */
object SyntaxLexer {

    @Suppress("CyclomaticComplexMethod")
    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            when {
                input[i].isWhitespace() -> i++
                input[i] == '[' -> {
                    tokens.add(Token(TokenType.LBRACKET, "["))
                    i++
                }
                input[i] == ']' -> {
                    tokens.add(Token(TokenType.RBRACKET, "]"))
                    i++
                }
                input[i] == ':' -> {
                    tokens.add(Token(TokenType.COLON, ":"))
                    i++
                }
                input[i].isDigit() -> {
                    val start = i
                    while (i < input.length && input[i].isDigit()) i++
                    tokens.add(Token(TokenType.NUMBER, input.substring(start, i)))
                }
                input[i].isLetter() -> {
                    val start = i
                    while (i < input.length && (input[i].isLetterOrDigit() || input[i] == '_')) i++
                    val word = input.substring(start, i)
                    val type = when (word.uppercase()) {
                        "LEMMA", "POS", "WITHIN" -> TokenType.KEYWORD
                        "WORDS" -> TokenType.WORDS
                        else -> TokenType.VALUE
                    }
                    tokens.add(Token(type, word))
                }
                else -> i++ // skip unknown characters
            }
        }
        tokens.add(Token(TokenType.EOF, ""))
        return tokens
    }
}

/**
 * Parses a list of [Token]s into a [SyntaxNode] AST.
 *
 * Grammar:
 * ```
 * query    = clause+
 * clause   = '[' keyword ':' value ']'
 *          | '[' 'WITHIN' number 'WORDS' ']'
 *          | text
 * keyword  = 'LEMMA' | 'POS'
 * ```
 */
object SyntaxParser {

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    fun parse(tokens: List<Token>): SyntaxNode {
        val clauses = mutableListOf<SyntaxNode>()
        var i = 0

        while (i < tokens.size && tokens[i].type != TokenType.EOF) {
            if (tokens[i].type == TokenType.LBRACKET) {
                i++ // consume '['
                val keyword = tokens.getOrNull(i) ?: break

                if (keyword.type == TokenType.KEYWORD && keyword.value.equals("WITHIN", ignoreCase = true)) {
                    // [WITHIN n WORDS]
                    i++ // consume 'WITHIN'
                    val num = tokens.getOrNull(i)
                    i++ // consume number
                    i++ // consume 'WORDS'
                    i++ // consume ']'
                    val distance = num?.value?.toIntOrNull() ?: 1
                    // Attach proximity to the last two clauses
                    if (clauses.size >= 2) {
                        val right = clauses.removeLast()
                        val left = clauses.removeLast()
                        clauses.add(SyntaxNode.ProximityNode(left, right, distance))
                    }
                } else if (keyword.type == TokenType.KEYWORD || keyword.type == TokenType.VALUE) {
                    i++ // consume keyword
                    i++ // consume ':'
                    val value = tokens.getOrNull(i)?.value ?: ""
                    i++ // consume value
                    i++ // consume ']'

                    when (keyword.value.uppercase()) {
                        "LEMMA" -> clauses.add(SyntaxNode.LemmaNode(value))
                        "POS" -> clauses.add(SyntaxNode.PosNode(value))
                        else -> clauses.add(SyntaxNode.TextNode(value))
                    }
                } else {
                    i++
                }
            } else if (tokens[i].type == TokenType.VALUE) {
                clauses.add(SyntaxNode.TextNode(tokens[i].value))
                i++
            } else {
                i++
            }
        }

        return when {
            clauses.isEmpty() -> SyntaxNode.TextNode("")
            clauses.size == 1 -> clauses[0]
            else -> SyntaxNode.AndNode(clauses)
        }
    }

    /**
     * Convenience: tokenize + parse in one call.
     */
    fun parseQuery(query: String): SyntaxNode {
        val tokens = SyntaxLexer.tokenize(query)
        return parse(tokens)
    }
}

/**
 * Converts a [SyntaxNode] AST into SQL query conditions.
 *
 * This builder produces conditions that can be combined with existing
 * FTS5 and morphology table queries.
 */
object SyntaxQueryBuilder {

    /**
     * Builds a SQL WHERE clause fragment from a [SyntaxNode].
     *
     * @return A pair of (SQL condition string, list of bind parameters).
     */
    fun buildQuery(node: SyntaxNode): Pair<String, List<String>> = when (node) {
        is SyntaxNode.LemmaNode -> {
            val sql = "v.global_verse_id IN (SELECT global_verse_id FROM word_occurrences WHERE strongs_number = ?)"
            sql to listOf(node.strongsNumber)
        }
        is SyntaxNode.PosNode -> {
            val sql = "v.global_verse_id IN (SELECT verse_id FROM morphology WHERE pos = ?)"
            sql to listOf(node.partOfSpeech)
        }
        is SyntaxNode.ProximityNode -> {
            val (leftSql, leftParams) = buildQuery(node.left)
            val (rightSql, rightParams) = buildQuery(node.right)
            val sql = "($leftSql) AND ($rightSql)"
            sql to (leftParams + rightParams)
        }
        is SyntaxNode.AndNode -> {
            val parts = node.children.map { buildQuery(it) }
            val sql = parts.joinToString(" AND ") { "(${it.first})" }
            val params = parts.flatMap { it.second }
            sql to params
        }
        is SyntaxNode.TextNode -> {
            val sql = "v.global_verse_id IN (SELECT rowid FROM fts_verses WHERE fts_verses MATCH ?)"
            sql to listOf(node.text)
        }
    }
}
