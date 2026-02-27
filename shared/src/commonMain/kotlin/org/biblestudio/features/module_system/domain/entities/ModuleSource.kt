package org.biblestudio.features.module_system.domain.entities

/**
 * The source from which a module is installed.
 */
sealed class ModuleSource {
    /** A SWORD Project module (.conf + compressed data). */
    data class Sword(val confPath: String, val dataPath: String) : ModuleSource()

    /** An OSIS XML file. */
    data class Osis(val xmlPath: String) : ModuleSource()

    /** A USFM text file set. */
    data class Usfm(val directoryPath: String) : ModuleSource()

    /** A custom zip archive containing module data. */
    data class CustomZip(val zipPath: String) : ModuleSource()
}
