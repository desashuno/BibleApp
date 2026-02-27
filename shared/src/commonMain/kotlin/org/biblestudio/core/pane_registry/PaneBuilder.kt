package org.biblestudio.core.pane_registry

/**
 * Factory function invoked by [PaneRegistry.build] to construct a pane's
 * content composable.
 *
 * The [config] map carries any runtime parameters the pane needs
 * (e.g., `"verseId" → "1001001"`).
 *
 * Note: this is declared as a plain function type in `shared/` (no @Composable
 * annotation) so it can be referenced from non-Compose modules. The actual
 * composable builders are registered in `composeApp/` where Compose is available.
 */
typealias PaneBuilder = (config: Map<String, String>) -> Unit
