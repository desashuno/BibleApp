package org.biblestudio.ui.i18n

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource as composeStringResource

/**
 * Portable `stringResource` wrapper that delegates to
 * Compose Multiplatform's `org.jetbrains.compose.resources.stringResource`.
 *
 * Usage:
 * ```kotlin
 * val label = localizedString(Res.string.app_name)
 * ```
 *
 * This indirection provides a single import site if we ever need to
 * swap the underlying resource system (e.g. for preview/test stubs).
 */
@Composable
fun localizedString(resource: StringResource): String = composeStringResource(resource)

/**
 * Formatted variant with positional arguments.
 */
@Composable
fun localizedString(resource: StringResource, vararg args: Any): String = composeStringResource(resource, *args)
