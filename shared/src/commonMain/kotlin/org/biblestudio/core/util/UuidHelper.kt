package org.biblestudio.core.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Generates a random UUID v4 string (e.g. "550e8400-e29b-41d4-a716-446655440000"). */
@OptIn(ExperimentalUuidApi::class)
fun generateUuid(): String = Uuid.random().toString()
