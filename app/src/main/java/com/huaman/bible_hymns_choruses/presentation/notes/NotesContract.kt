package com.huaman.bible_hymns_choruses.presentation.notes

import androidx.compose.runtime.Immutable
import com.huaman.bible_hymns_choruses.domain.model.NoteItem

@Immutable
data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<NoteItem> = emptyList(),
    val editingNoteId: Int = 0,
    val editingCreatedAt: Long? = null,
    val draftTitle: String = "",
    val draftContent: String = "",
    val draftImagePaths: List<String> = emptyList(),
    val draftAudioPaths: List<String> = emptyList(),
    val isRecordingAudio: Boolean = false,
    val recordingElapsedSeconds: Int = 0,
    val playingAudioPath: String? = null,
    val playingElapsedSeconds: Int = 0,
    val playingDurationSeconds: Int = 0,
    val previewImagePath: String? = null
)

sealed interface NotesIntent {
    data object PrepareNewNote : NotesIntent
    data class LoadNoteForEdit(val noteId: Int) : NotesIntent
    data class UpdateTitle(val title: String) : NotesIntent
    data class UpdateContent(val content: String) : NotesIntent
    data class AttachImageUri(val uriString: String) : NotesIntent
    data class RemoveImagePath(val path: String) : NotesIntent
    data object StartAudioRecording : NotesIntent
    data object StopAudioRecording : NotesIntent
    data object CancelAudioRecording : NotesIntent
    data class RemoveAudioPath(val path: String) : NotesIntent
    data object SaveNote : NotesIntent
    data class DeleteNote(val id: Int) : NotesIntent
    data class TogglePlayAudio(val path: String) : NotesIntent
    data class OpenImagePreview(val path: String?) : NotesIntent
}

sealed interface NotesEffect {
    data class ShowMessage(val message: String) : NotesEffect
    data object NoteSaved : NotesEffect
}
