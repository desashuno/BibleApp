package org.biblestudio.core.data_manager.model

/**
 * Types of data modules that can be managed by the DataManager.
 */
enum class DataModuleType(val value: String, val displayName: String) {
    Bible("bible", "Bible"),
    Commentary("commentary", "Commentary"),
    Dictionary("dictionary", "Dictionary"),
    Morphology("morphology", "Morphology"),
    CrossReferences("cross_references", "Cross References"),
    Geography("geography", "Geography"),
    Entities("entities", "Entities"),
    Timeline("timeline", "Timeline");

    companion object {
        fun fromString(value: String): DataModuleType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: Bible
    }
}
