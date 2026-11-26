package com.huaman.bible_hymns_choruses.presentation.notes

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.domain.usecase.ManageNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val manageNotesUseCase: ManageNotesUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<NotesEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<NotesEffect> = _effects.asSharedFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        viewModelScope.launch {
            manageNotesUseCase.observeAll().collect { list ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        notes = list
                    )
                }
            }
        }
    }

    fun onIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.OpenNewNote -> {
                _uiState.update {
                    it.copy(
                        isEditorVisible = true,
                        editingNoteId = 0,
                        editingCreatedAt = null,
                        draftTitle = "",
                        draftContent = "",
                        draftImagePaths = emptyList(),
                        draftAudioPaths = emptyList()
                    )
                }
            }

            is NotesIntent.OpenEditNote -> {
                val note = intent.note
                _uiState.update {
                    it.copy(
                        isEditorVisible = true,
                        editingNoteId = note.id,
                        editingCreatedAt = note.createdAt,
                        draftTitle = note.title,
                        draftContent = note.content,
                        draftImagePaths = note.imagePaths,
                        draftAudioPaths = note.audioPaths
                    )
                }
            }

            is NotesIntent.DismissEditor -> {
                _uiState.update { it.copy(isEditorVisible = false) }
            }

            is NotesIntent.UpdateTitle -> {
                _uiState.update { it.copy(draftTitle = intent.title) }
            }

            is NotesIntent.UpdateContent -> {
                _uiState.update { it.copy(draftContent = intent.content) }
            }

            is NotesIntent.AttachImageUri -> {
                viewModelScope.launch {
                    val savedPath = copyUriToInternalStorage(
                        uri = Uri.parse(intent.uriString),
                        subdir = "notes_images",
                        defaultExt = "jpg"
                    )
                    if (savedPath != null) {
                        _uiState.update {
                            it.copy(draftImagePaths = it.draftImagePaths + savedPath)
                        }
                    } else {
                        _effects.tryEmit(NotesEffect.ShowMessage("No se pudo adjuntar la imagen"))
                    }
                }
            }

            is NotesIntent.RemoveImagePath -> {
                _uiState.update {
                    it.copy(draftImagePaths = it.draftImagePaths - intent.path)
                }
            }

            is NotesIntent.AttachAudioUri -> {
                viewModelScope.launch {
                    val savedPath = copyUriToInternalStorage(
                        uri = Uri.parse(intent.uriString),
                        subdir = "notes_audios",
                        defaultExt = "m4a"
                    )
                    if (savedPath != null) {
                        _uiState.update {
                            it.copy(draftAudioPaths = it.draftAudioPaths + savedPath)
                        }
                    } else {
                        _effects.tryEmit(NotesEffect.ShowMessage("No se pudo adjuntar el audio"))
                    }
                }
            }

            is NotesIntent.RemoveAudioPath -> {
                if (_uiState.value.playingAudioPath == intent.path) {
                    stopAudioPlayback()
                }
                _uiState.update {
                    it.copy(draftAudioPaths = it.draftAudioPaths - intent.path)
                }
            }

            is NotesIntent.SaveNote -> {
                val state = _uiState.value
                val hasAnyContent = state.draftTitle.isNotBlank() ||
                    state.draftContent.isNotBlank() ||
                    state.draftImagePaths.isNotEmpty() ||
                    state.draftAudioPaths.isNotEmpty()

                if (!hasAnyContent) {
                    _effects.tryEmit(NotesEffect.ShowMessage("Escribe un apunte o adjunta un archivo"))
                    return
                }

                viewModelScope.launch {
                    manageNotesUseCase.save(
                        id = state.editingNoteId,
                        title = state.draftTitle,
                        content = state.draftContent,
                        imagePaths = state.draftImagePaths,
                        audioPaths = state.draftAudioPaths,
                        createdAt = state.editingCreatedAt
                    )
                    _uiState.update { it.copy(isEditorVisible = false) }
                    _effects.tryEmit(NotesEffect.ShowMessage("Apunte guardado"))
                }
            }

            is NotesIntent.DeleteNote -> {
                viewModelScope.launch {
                    manageNotesUseCase.removeById(intent.id)
                    _effects.tryEmit(NotesEffect.ShowMessage("Apunte eliminado"))
                }
            }

            is NotesIntent.TogglePlayAudio -> {
                toggleAudioPlayback(intent.path)
            }
        }
    }

    private fun toggleAudioPlayback(path: String) {
        if (_uiState.value.playingAudioPath == path) {
            stopAudioPlayback()
            return
        }
        stopAudioPlayback()
        try {
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    stopAudioPlayback()
                }
                start()
            }
            mediaPlayer = player
            _uiState.update { it.copy(playingAudioPath = path) }
        } catch (_: Exception) {
            stopAudioPlayback()
            _effects.tryEmit(NotesEffect.ShowMessage("No se pudo reproducir el audio"))
        }
    }

    private fun stopAudioPlayback() {
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _uiState.update { it.copy(playingAudioPath = null) }
    }

    private suspend fun copyUriToInternalStorage(
        uri: Uri,
        subdir: String,
        defaultExt: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(appContext.filesDir, subdir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val originalName = resolveDisplayName(uri)
            val safeName = originalName
                ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
                ?.takeIf { it.isNotBlank() }
                ?: "media_${System.currentTimeMillis()}.$defaultExt"
            val destFile = File(dir, "${System.currentTimeMillis()}_$safeName")
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            destFile.absolutePath
        }.getOrNull()
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull()
    }

    override fun onCleared() {
        stopAudioPlayback()
        super.onCleared()
    }
}
