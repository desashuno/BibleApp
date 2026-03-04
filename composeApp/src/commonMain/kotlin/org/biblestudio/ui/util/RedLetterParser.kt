package org.biblestudio.ui.util

/**
 * Extracts character ranges in [plainText] that correspond to `<wj>...</wj>` segments in [htmlText].
 *
 * The parser is tolerant: malformed/unclosed tags are handled gracefully and yield best-effort ranges.
 */
fun extractRedLetterRanges(htmlText: String?, plainText: String): List<IntRange> {
    if (htmlText.isNullOrBlank()) return emptyList()
    if (!htmlText.contains("<wj", ignoreCase = true)) return emptyList()

    val ranges = mutableListOf<IntRange>()
    var plainOffset = 0
    var idx = 0
    var currentWjStart: Int? = null

    while (idx < htmlText.length) {
        val ch = htmlText[idx]
        if (ch == '<') {
            val end = htmlText.indexOf('>', startIndex = idx)
            if (end < 0) {
                break
            }

            val tagBody = htmlText.substring(idx + 1, end).trim()
            val normalized = tagBody.lowercase()
            when {
                normalized == "wj" || normalized.startsWith("wj ") -> {
                    if (currentWjStart == null) {
                        currentWjStart = plainOffset
                    }
                }
                normalized == "/wj" -> {
                    val start = currentWjStart
                    if (start != null && plainOffset > start) {
                        ranges += start..(plainOffset - 1)
                    }
                    currentWjStart = null
                }
            }

            idx = end + 1
            continue
        }

        plainOffset++
        idx++
    }

    // Graceful fallback for malformed/unclosed tags
    val danglingStart = currentWjStart
    if (danglingStart != null && plainOffset > danglingStart) {
        ranges += danglingStart..(plainOffset - 1)
    }

    if (plainText.isEmpty() || ranges.isEmpty()) return emptyList()

    val maxIndex = plainText.lastIndex
    return ranges
        .mapNotNull { range ->
            val start = range.first.coerceAtLeast(0).coerceAtMost(maxIndex)
            val end = range.last.coerceAtLeast(0).coerceAtMost(maxIndex)
            if (start <= end) start..end else null
        }
}
