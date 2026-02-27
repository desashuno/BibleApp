package org.biblestudio.features.workspace.domain.model

import kotlinx.serialization.Serializable

/** Orientation of a split within the workspace layout tree. */
@Serializable
enum class SplitAxis {
    /** Side-by-side (left | right). */
    Horizontal,

    /** Stacked (top / bottom). */
    Vertical
}
