package org.biblestudio.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import io.github.aakira.napier.Napier
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus

/**
 * Default implementation of [RootComponent] backed by Decompose's
 * [StackNavigation].
 *
 * @param componentContext The Decompose lifecycle owner.
 * @param verseBus The shared event bus for deep link resolution.
 * @param initialConfig The initial screen to show (defaults to [RootConfig.Workspace]).
 */
internal class DefaultRootComponent(
    componentContext: ComponentContext,
    private val verseBus: VerseBus,
    initialConfig: RootConfig = RootConfig.Workspace
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootConfig>()

    override val childStack: Value<ChildStack<RootConfig, RootChild>> =
        childStack(
            source = navigation,
            serializer = RootConfig.serializer(),
            initialConfiguration = resolveInitialConfig(initialConfig),
            handleBackButton = true,
            childFactory = ::createChild
        )

    init {
        // If started with a deep link, publish the event
        if (initialConfig is RootConfig.DeepLink) {
            resolveDeepLink(initialConfig)
        }
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun navigateTo(config: RootConfig) {
        when (config) {
            is RootConfig.DeepLink -> {
                resolveDeepLink(config)
                navigation.push(RootConfig.Workspace)
            }
            else -> navigation.push(config)
        }
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun onBack() {
        navigation.pop()
    }

    private fun createChild(
        config: RootConfig,
        @Suppress("UnusedParameter") componentContext: ComponentContext
    ): RootChild = when (config) {
        is RootConfig.Workspace -> RootChild.Workspace
        is RootConfig.Settings -> RootChild.Settings
        is RootConfig.Import -> RootChild.Import
        is RootConfig.DeepLink -> {
            // Deep links resolve to Workspace
            RootChild.Workspace
        }
    }

    /**
     * Resolves a [RootConfig.DeepLink] by extracting the initial config
     * (always [RootConfig.Workspace]) for the stack.
     */
    private fun resolveInitialConfig(config: RootConfig): RootConfig = when (config) {
        is RootConfig.DeepLink -> RootConfig.Workspace
        else -> config
    }

    /**
     * Resolves a deep link by publishing a [LinkEvent.VerseSelected] to the [VerseBus].
     */
    private fun resolveDeepLink(deepLink: RootConfig.DeepLink) {
        Napier.i("Resolving deep link to verse ${deepLink.globalVerseId}")
        verseBus.publish(LinkEvent.VerseSelected(deepLink.globalVerseId))
    }
}
