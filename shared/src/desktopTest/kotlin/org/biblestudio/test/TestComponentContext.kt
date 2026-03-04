package org.biblestudio.test

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

/**
 * Creates a [ComponentContext] suitable for unit tests.
 */
fun testComponentContext(): ComponentContext {
    val lifecycle = LifecycleRegistry()
    return DefaultComponentContext(lifecycle = lifecycle)
}
