package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.core.study.CrossReference
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.passage_guide.component.PassageGuideState
import org.biblestudio.features.passage_guide.domain.entities.Outline
import org.biblestudio.features.passage_guide.domain.entities.PassageReport
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.core.study.LexiconEntry

@OptIn(ExperimentalTestApi::class)
class PassageGuidePaneTest {

    private val testReport = PassageReport(
        verseId = 1_001_001,
        verseText = "In the beginning God created the heavens and the earth.",
        crossReferences = listOf(
            CrossReference(1, 1_001_001, 43_001_001, "parallel", 0.95)
        ),
        outlines = listOf(
            Outline(1, 1_001_001, 1_001_031, "Creation Account", "Gen 1:1-31", "Study Bible")
        ),
        keyWords = listOf(
            LexiconEntry("H1254", "בָּרָא", "bara", "to create", null)
        ),
        commentaryEntries = listOf(
            ResourceEntry(1, "res-1", 1_001_001, "Commentary on Gen 1:1", 1)
        ),
        userNotes = listOf(
            Note(
                "note-1",
                1_001_001,
                "My Note",
                "Note content",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01",
                deviceId = "dev1"
            )
        ),
        morphologyWords = emptyList()
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun PassageGuidePane_rendersSectionHeaders() = runComposeUiTest {
        val flow = MutableStateFlow(
            PassageGuideState(
                report = testReport,
                expandedSections = setOf("crossRefs", "outlines", "keyWords", "commentary", "notes")
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            PassageGuidePane(
                stateFlow = flow,
                onRefSelected = {},
                onWordSelected = {},
                onSectionToggle = {}
            )
        }

        onNodeWithText("Passage Guide").assertIsDisplayed()
        onNodeWithText("Cross-References").assertIsDisplayed()
        onNodeWithText("Outlines").assertIsDisplayed()
        onNodeWithText("Key Words").assertIsDisplayed()
        onNodeWithText("Commentary").assertIsDisplayed()
        onNodeWithText("Notes").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun PassageGuidePane_emptyStateShowsPrompt() = runComposeUiTest {
        val flow = MutableStateFlow(PassageGuideState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            PassageGuidePane(
                stateFlow = flow,
                onRefSelected = {},
                onWordSelected = {},
                onSectionToggle = {}
            )
        }

        onNodeWithText("Select a verse to see the passage guide").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun PassageGuidePane_rendersExpandedContent() = runComposeUiTest {
        val flow = MutableStateFlow(
            PassageGuideState(
                report = testReport,
                expandedSections = setOf("crossRefs", "outlines", "commentary", "notes")
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            PassageGuidePane(
                stateFlow = flow,
                onRefSelected = {},
                onWordSelected = {},
                onSectionToggle = {}
            )
        }

        // Verse text
        onNodeWithText("In the beginning God created the heavens and the earth.").assertIsDisplayed()
        // Cross-ref content
        onNodeWithText("John 1:1 (parallel)", substring = true).assertIsDisplayed()
        // Outline content
        onNodeWithText("Creation Account").assertIsDisplayed()
        // Commentary content
        onNodeWithText("Commentary on Gen 1:1").assertIsDisplayed()
        // Notes content
        onNodeWithText("My Note").assertIsDisplayed()
    }
}
