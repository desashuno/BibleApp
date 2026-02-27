package org.biblestudio.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * BibleStudio colour tokens.
 *
 * Pane-category colours and semantic colours are exposed as top-level
 * properties so they can be used outside `MaterialTheme.colorScheme`.
 */
object AppColors {
    // ── Pane Category Accent Colours ──
    val PaneText = Color(0xFF5B7E6E)
    val PaneStudy = Color(0xFF6B5B8A)
    val PaneResource = Color(0xFF7A6B5A)
    val PaneWriting = Color(0xFF5A6B7A)
    val PaneTool = Color(0xFF7A5A5A)
    val PaneMedia = Color(0xFF5A7A6B)

    // ── Semantic ──
    val SuccessLight = Color(0xFF2E7D32)
    val SuccessDark = Color(0xFF81C784)
    val WarningLight = Color(0xFFE65100)
    val WarningDark = Color(0xFFFFB74D)
    val InfoLight = Color(0xFF0277BD)
    val InfoDark = Color(0xFF4FC3F7)

    // ── Highlight Palette ──
    val HighlightYellow = Color(0xFFFFF3B0)
    val HighlightGreen = Color(0xFFC8E6C9)
    val HighlightBlue = Color(0xFFBBDEFB)
    val HighlightPink = Color(0xFFF8BBD0)
    val HighlightOrange = Color(0xFFFFE0B2)
    val HighlightPurple = Color(0xFFE1BEE7)
    val HighlightRed = Color(0xFFFFCDD2)
    val HighlightTeal = Color(0xFFB2DFDB)

    /** Ordered list of default highlight colours. */
    val highlights = listOf(
        HighlightYellow,
        HighlightGreen,
        HighlightBlue,
        HighlightPink,
        HighlightOrange,
        HighlightPurple,
        HighlightRed,
        HighlightTeal
    )
}
