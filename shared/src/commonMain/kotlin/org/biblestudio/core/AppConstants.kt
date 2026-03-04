package org.biblestudio.core

/**
 * Application-wide constants shared across multiple features.
 *
 * Centralizes magic values that were previously scattered across
 * component implementations (font sizes, device IDs, setting
 * types/categories, auto-save timing, highlight styles).
 */
object AppConstants {
    const val FONT_SIZE_DEFAULT = 16
    const val FONT_SIZE_MIN = 12
    const val FONT_SIZE_MAX = 28

    const val AUTO_SAVE_DEBOUNCE_MS = 2_000L

    const val DEVICE_ID_LOCAL = "local"
    const val DEVICE_ID_IMPORT = "import"

    const val HIGHLIGHT_STYLE_BACKGROUND = "background"

    object SettingType {
        const val LONG = "long"
        const val INT = "int"
        const val BOOLEAN = "boolean"
        const val STRING = "string"
    }

    object SettingCategory {
        const val READING = "reading"
        const val DISPLAY = "display"
        const val WORKSPACE = "workspace"
    }
}
