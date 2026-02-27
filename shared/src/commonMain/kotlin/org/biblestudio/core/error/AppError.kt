package org.biblestudio.core.error

/**
 * Base class for all BibleStudio domain errors.
 *
 * Each subtype carries a user-facing message and a debug-level message.
 * Components expose errors via `StateFlow` — they never throw.
 */
sealed class AppError(
    open val userMessage: String,
    open val debugMessage: String
) {
    data class Database(
        override val userMessage: String,
        override val debugMessage: String
    ) : AppError(userMessage, debugMessage)

    data class Network(
        override val userMessage: String,
        override val debugMessage: String
    ) : AppError(userMessage, debugMessage)

    data class FileImport(
        override val userMessage: String,
        override val debugMessage: String
    ) : AppError(userMessage, debugMessage)

    data class Validation(
        override val userMessage: String,
        override val debugMessage: String
    ) : AppError(userMessage, debugMessage)

    data class Permission(
        override val userMessage: String,
        override val debugMessage: String
    ) : AppError(userMessage, debugMessage)
}
