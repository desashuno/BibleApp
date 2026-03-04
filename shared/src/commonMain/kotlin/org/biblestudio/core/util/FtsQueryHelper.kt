package org.biblestudio.core.util

/** Characters that are special in FTS5 syntax and break match queries. */
val FTS_SPECIAL_CHARS: Set<Char> = setOf('(', ')', '^', '*', '{', '}', '[', ']', '"', '+', '-', ':')

/**
 * Returns `true` when [query] contains characters that would break an FTS5 MATCH expression.
 */
fun hasFtsSpecialChars(query: String): Boolean = query.any { it in FTS_SPECIAL_CHARS }
