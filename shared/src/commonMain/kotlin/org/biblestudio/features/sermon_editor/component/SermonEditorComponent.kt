package org.biblestudio.features.sermon_editor.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection
import org.biblestudio.features.sermon_editor.domain.entities.SermonStatus

/**
 * UI state for the Sermon Editor pane.
 */
data class SermonEditorState(
    val sermons: List<Sermon> = emptyList(),
    val activeSermon: Sermon? = null,
    val sections: List<SermonSection> = emptyList(),
    val editTitle: String = "",
    val editScriptureRef: String = "",
    val status: SermonStatus = SermonStatus.Draft,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val wordCount: Int = 0,
    val estimatedMinutes: Int = 0,
    val searchQuery: String = "",
    val searchResults: List<Sermon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Component for the Sermon Editor feature.
 */
interface SermonEditorComponent {
    val state: StateFlow<SermonEditorState>
    fun onSermonSelected(uuid: String)
    fun onTitleChanged(title: String)
    fun onScriptureRefChanged(ref: String)
    fun onStatusChanged(status: SermonStatus)
    fun onNewSermon()
    fun onDeleteSermon(uuid: String)
    fun onSectionContentChanged(sectionId: Long, content: String)
    fun onAddSection(type: String)
    fun onDeleteSection(sectionId: Long)
    fun onMoveSectionUp(sectionId: Long)
    fun onMoveSectionDown(sectionId: Long)
    fun onSearchQueryChanged(query: String)
}
