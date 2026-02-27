package org.biblestudio.core.verse_bus

/**
 * Events broadcast through the [VerseBus] for cross-pane communication.
 *
 * When a user interacts with a verse reference in any pane, the originating
 * component publishes a [LinkEvent]. All other panes that collect from the
 * VerseBus react accordingly (e.g., scrolling, loading cross-references).
 */
sealed class LinkEvent {

    /**
     * A verse has been selected or navigated to.
     *
     * @param globalVerseId The BBCCCVVV-encoded verse identifier.
     */
    data class VerseSelected(val globalVerseId: Int) : LinkEvent()

    /**
     * A Strong's number has been selected for word study.
     *
     * @param strongsNumber The Strong's concordance number (e.g., "H1234", "G5678").
     */
    data class StrongsSelected(val strongsNumber: String) : LinkEvent()

    /**
     * A passage range has been selected (e.g., from a reading plan or cross-reference).
     *
     * @param startVerseId The BBCCCVVV-encoded start verse.
     * @param endVerseId The BBCCCVVV-encoded end verse.
     */
    data class PassageSelected(
        val startVerseId: Int,
        val endVerseId: Int
    ) : LinkEvent()

    /**
     * A resource entry has been selected for display.
     *
     * @param resourceId The unique identifier of the resource.
     * @param entryId Optional entry within the resource.
     */
    data class ResourceSelected(
        val resourceId: String,
        val entryId: String? = null
    ) : LinkEvent()

    /**
     * A search result has been selected, requesting navigation.
     *
     * @param globalVerseId The BBCCCVVV-encoded verse from the search result.
     * @param query The original search query for context.
     */
    data class SearchResult(
        val globalVerseId: Int,
        val query: String
    ) : LinkEvent()
}
