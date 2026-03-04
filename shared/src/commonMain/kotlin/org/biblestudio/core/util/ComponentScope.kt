package org.biblestudio.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a standard [CoroutineScope] for Decompose components.
 *
 * Uses [SupervisorJob] so that a single child failure does not cancel
 * sibling coroutines, and [Dispatchers.Default] for CPU-bound work.
 */
fun componentScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
