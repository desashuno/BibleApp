package org.biblestudio.core.data_manager.model

/**
 * Lifecycle status of a data module.
 */
enum class DataModuleStatus(val value: String, val displayName: String) {
    Available("available", "Available"),
    Downloading("downloading", "Downloading"),
    Installing("installing", "Installing"),
    Installed("installed", "Installed"),
    UpdateAvailable("update_available", "Update Available"),
    Removing("removing", "Removing"),
    Error("error", "Error");

    companion object {
        fun fromString(value: String): DataModuleStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: Available
    }
}
