package org.biblestudio.core.data_manager.parsers

/**
 * A parsed verse from any Bible format.
 */
data class ParsedVerse(
    val bookNumber: Int,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String,
    val htmlText: String? = null,
    val lemmaRefs: List<String> = emptyList()
)

/**
 * A parsed book from any Bible format.
 */
data class ParsedBook(
    val number: Int,
    val name: String,
    val testament: String,
    val chapters: Map<Int, List<ParsedVerse>>
)

/**
 * Result of parsing a module source.
 */
data class ParseResult(
    val moduleName: String,
    val abbreviation: String,
    val language: String,
    val books: List<ParsedBook>,
    val errors: List<String> = emptyList()
)

/**
 * Validation result for a parsed module.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun valid(): ValidationResult = ValidationResult(true, emptyList())
        fun invalid(errors: List<String>): ValidationResult = ValidationResult(false, errors)
    }
}
