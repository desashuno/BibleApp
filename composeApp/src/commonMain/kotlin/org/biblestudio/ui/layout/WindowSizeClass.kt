package org.biblestudio.ui.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive breakpoints aligned with the BibleStudio design system.
 *
 * | Class    | Width       | Shell    |
 * |----------|-------------|----------|
 * | Compact  | < 600 dp   | Mobile   |
 * | Medium   | 600–839 dp | Tablet   |
 * | Expanded | 840–1199 dp| Desktop  |
 * | Large    | ≥ 1200 dp  | Desktop  |
 */
enum class WindowSizeClass {
    Compact,
    Medium,
    Expanded,
    Large
    ;

    companion object {
        private val MEDIUM_BREAKPOINT = 600.dp
        private val EXPANDED_BREAKPOINT = 840.dp
        private val LARGE_BREAKPOINT = 1200.dp

        /** Determines the [WindowSizeClass] from the available [width]. */
        fun fromWidth(width: Dp): WindowSizeClass = when {
            width < MEDIUM_BREAKPOINT -> Compact
            width < EXPANDED_BREAKPOINT -> Medium
            width < LARGE_BREAKPOINT -> Expanded
            else -> Large
        }
    }
}
