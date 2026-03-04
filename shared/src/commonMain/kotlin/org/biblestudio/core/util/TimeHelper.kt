package org.biblestudio.core.util

import kotlinx.datetime.Clock

/**
 * Returns the current instant as an ISO-8601 string.
 */
fun nowIso(): String = Clock.System.now().toString()
