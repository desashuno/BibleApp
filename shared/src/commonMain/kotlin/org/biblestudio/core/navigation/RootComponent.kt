package org.biblestudio.core.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/**
 * Contract for the root-level navigation component.
 *
 * The root component owns a [ChildStack] driven by [RootConfig] entries.
 * UI layers observe [childStack] and render the active [RootChild].
 */
interface RootComponent {

    /** The observable child stack managed by Decompose. */
    val childStack: Value<ChildStack<RootConfig, RootChild>>

    /** Navigates to the given [config], pushing it onto the stack. */
    fun navigateTo(config: RootConfig)

    /** Pops the top entry from the stack, returning to the previous screen. */
    fun onBack()
}
