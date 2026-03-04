package org.biblestudio.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.test.testComponentContext

class DefaultRootComponentTest {

    private fun createComponent(
        verseBus: VerseBus = VerseBus(),
        initialConfig: RootConfig = RootConfig.Workspace
    ): DefaultRootComponent {
        val context = testComponentContext()
        return DefaultRootComponent(
            componentContext = context,
            verseBus = verseBus,
            initialConfig = initialConfig
        )
    }

    @Test
    fun `child stack starts with Workspace config`() {
        val component = createComponent()

        val activeChild = component.childStack.value.active
        assertIs<RootConfig.Workspace>(activeChild.configuration)
        assertIs<RootChild.Workspace>(activeChild.instance)
    }

    @Test
    fun `navigateTo pushes new child onto stack`() {
        val component = createComponent()

        component.navigateTo(RootConfig.Settings)

        val stack = component.childStack.value
        assertIs<RootChild.Settings>(stack.active.instance)
        assertEquals(2, stack.items.size)
    }

    @Test
    fun `deep link publishes to VerseBus and navigates to Workspace`() {
        val verseBus = VerseBus()
        val verseId = 1_001_001

        val component = createComponent(
            verseBus = verseBus,
            initialConfig = RootConfig.DeepLink(verseId)
        )

        // Verify the stack shows Workspace
        val activeChild = component.childStack.value.active
        assertIs<RootChild.Workspace>(activeChild.instance)

        // Verify the VerseBus received the event
        val event = verseBus.current
        assertIs<LinkEvent.VerseSelected>(event)
        assertEquals(verseId, event.globalVerseId)
    }
}
