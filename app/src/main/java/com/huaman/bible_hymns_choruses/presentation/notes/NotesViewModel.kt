package com.huaman.bible_hymns_choruses.presentation.notes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.domain.usecase.ManageNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val manageNotesUseCase: ManageNotesUseCase,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<NotesEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<NotesEffect> = _effects.asSharedFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var activeRecordingFile: File? = null
    private var recordingTimerJob: Job? = null

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
            is NotesIntent.PrepareNewNote -> {
                stopAudioRecording(discard = true)
                stopAudioPlayback()
                _uiState.update {
                    it.copy(
                        editingNoteId = 0,
                        editingCreatedAt = null,
                        draftTitle = "",
                        draftContent = "",
                        draftImagePaths = emptyList(),
                        draftAudioPaths = emptyList(),
                        isRecordingAudio = false,
                        recordingElapsedSeconds = 0
                    )
                }
            }

            is NotesIntent.LoadNoteForEdit -> {
                stopAudioRecording(discard = true)
                stopAudioPlayback()
                if (intent.noteId == 0) {
                    onIntent(NotesIntent.PrepareNewNote)
                    return
                }
                val existing = _uiState.value.notes.firstOrNull { it.id == intent.noteId }
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            editingNoteId = existing.id,
                            editingCreatedAt = existing.createdAt,
                            draftTitle = existing.title,
                            draftContent = existing.content,
                            draftImagePaths = existing.imagePaths,
                            draftAudioPaths = existing.audioPaths,
                            isRecordingAudio = false,
                            recordingElapsedSeconds = 0
                        )
                    }
                }
            }

            is NotesIntent.UpdateTitle -> {
                _uiState.update { it.copy(draftTitle = intent.title) }
            }

            is NotesIntent.UpdateContent -> {
                _uiState.update { it.copy(draftContent = intent.content) }
            }

            is NotesIntent.AttachImageUri -> {
                viewModelScope.launch {
                    val savedPath = compressAndSaveImageToInternalStorage(Uri.parse(intent.uriString))
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

            is NotesIntent.StartAudioRecording -> {
                startAudioRecording()
            }

            is NotesIntent.StopAudioRecording -> {
                stopAudioRecording(discard = false)
            }

            is NotesIntent.CancelAudioRecording -> {
                stopAudioRecording(discard = true)
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
                if (_uiState.value.isRecordingAudio) {
                    stopAudioRecording(discard = false)
                }
                stopAudioPlayback()

                val state = _uiState.value
                val hasAnyContent = state.draftTitle.isNotBlank() ||
                    state.draftContent.isNotBlank() ||
                    state.draftImagePaths.isNotEmpty() ||
                    state.draftAudioPaths.isNotEmpty()

                if (!hasAnyContent) {
                    _effects.tryEmit(NotesEffect.ShowMessage("Escribe un apunte, graba un audio o adjunta una imagen"))
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
                    _effects.tryEmit(NotesEffect.ShowMessage("Apunte guardado"))
                    _effects.tryEmit(NotesEffect.NoteSaved)
                }
            }

            is NotesIntent.DeleteNote -> {
                val target = _uiState.value.notes.firstOrNull { it.id == intent.id }
                if (target != null && _uiState.value.playingAudioPath in target.audioPaths) {
                    stopAudioPlayback()
                }
                viewModelScope.launch(Dispatchers.IO) {
                    target?.let { note ->
                        (note.imagePaths + note.audioPaths).forEach { path ->
                            runCatching { File(path).delete() }
                        }
                    }
                    manageNotesUseCase.removeById(intent.id)
                    _effects.tryEmit(NotesEffect.ShowMessage("Apunte eliminado"))
                }
            }

            is NotesIntent.TogglePlayAudio -> {
                toggleAudioPlayback(intent.path)
            }

            is NotesIntent.OpenImagePreview -> {
                _uiState.update { it.copy(previewImagePath = intent.path) }
            }
        }
    }

    /**
     * Records ultra-lightweight voice notes in MPEG-4 / AAC-LC mono @ 16 kHz / 24 kbps (~180 KB per minute)
     * so users can record dozens of voice notes per Apunte without bloating storage.
     */
    private fun startAudioRecording() {
        if (_uiState.value.isRecordingAudio) return
        stopAudioPlayback()

        runCatching {
            val dir = File(appContext.filesDir, "notes_audios")
            if (!dir.exists()) dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outFile = File(dir, "Voz_$timestamp.m4a")

            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(24000)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            activeRecordingFile = outFile
            _uiState.update {
                it.copy(
                    isRecordingAudio = true,
                    recordingElapsedSeconds = 0
                )
            }

            recordingTimerJob?.cancel()
            recordingTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000L)
                    _uiState.update { state ->
                        state.copy(recordingElapsedSeconds = state.recordingElapsedSeconds + 1)
                    }
                }
            }
        }.onFailure {
            stopAudioRecording(discard = true)
            _effects.tryEmit(NotesEffect.ShowMessage("No se pudo iniciar la grabación de voz"))
        }
    }

    private fun stopAudioRecording(discard: Boolean) {
        recordingTimerJob?.cancel()
        recordingTimerJob = null

        val recorder = mediaRecorder
        val recordedFile = activeRecordingFile
        mediaRecorder = null
        activeRecordingFile = null

        if (recorder != null) {
            runCatching { recorder.stop() }
            runCatching { recorder.release() }
        }

        if (discard) {
            runCatching { recordedFile?.delete() }
            _uiState.update {
                it.copy(
                    isRecordingAudio = false,
                    recordingElapsedSeconds = 0
                )
            }
        } else {
            val validPath = recordedFile
                ?.takeIf { it.exists() && it.length() > 0L }
                ?.absolutePath
            _uiState.update { state ->
                state.copy(
                    isRecordingAudio = false,
                    recordingElapsedSeconds = 0,
                    draftAudioPaths = if (validPath != null) {
                        state.draftAudioPaths + validPath
                    } else {
                        state.draftAudioPaths
                    }
                )
            }
        }
    }

    private fun toggleAudioPlayback(path: String) {
        if (_uiState.value.playingAudioPath == path) {
            stopAudioPlayback()
            return
        }
        if (_uiState.value.isRecordingAudio) {
            stopAudioRecording(discard = false)
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
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        _uiState.update { it.copy(playingAudioPath = null) }
    }

    /**
     * Downscales attached images to max 1280px longest side and compresses to JPEG 80% (~100-180 KB)
     * so dozens of images across many Apuntes keep the app fast and lightweight.
     */
    private suspend fun compressAndSaveImageToInternalStorage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(appContext.filesDir, "notes_images")
                if (!dir.exists()) dir.mkdirs()

                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, boundsOptions)
                }

                val maxDimension = 1280
                val longestSide = max(boundsOptions.outWidth, boundsOptions.outHeight)
                var sampleSize = 1
                while (longestSide / (sampleSize * 2) >= maxDimension) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decodedBitmap = appContext.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOptions)
                } ?: return@withContext null

                val finalBitmap = if (max(decodedBitmap.width, decodedBitmap.height) > maxDimension) {
                    val ratio = maxDimension.toFloat() / max(decodedBitmap.width, decodedBitmap.height).toFloat()
                    val targetW = (decodedBitmap.width * ratio).toInt().coerceAtLeast(1)
                    val targetH = (decodedBitmap.height * ratio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(decodedBitmap, targetW, targetH, true).also {
                        if (it != decodedBitmap) decodedBitmap.recycle()
                    }
                } else {
                    decodedBitmap
                }

                val destFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
                FileOutputStream(destFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                finalBitmap.recycle()
                destFile.absolutePath
            }.getOrNull()
        }

    override fun onCleared() {
        stopAudioRecording(discard = true)
        stopAudioPlayback()
        super.onCleared()
    }
}
