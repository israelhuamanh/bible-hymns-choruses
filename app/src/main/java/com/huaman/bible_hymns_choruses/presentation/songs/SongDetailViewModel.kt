package com.huaman.bible_hymns_choruses.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.usecase.GetSongDetailUseCase
import com.huaman.bible_hymns_choruses.domain.usecase.ManageBookmarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val getSongDetailUseCase: GetSongDetailUseCase,
    private val manageBookmarksUseCase: ManageBookmarksUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SongDetailEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<SongDetailEffect> = _effects.asSharedFlow()

    private val songIdFlow = MutableStateFlow<Int?>(null)

    init {
        observeSongDetail()
    }

    private fun observeSongDetail() {
        viewModelScope.launch {
            songIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { id ->
                    getSongDetailUseCase(id).flatMapLatest { detail ->
                        val bookmarkType = if (detail?.type == SongType.CHORUS) {
                            BookmarkType.CHORUS
                        } else {
                            BookmarkType.HYMN
                        }
                        combine(
                            manageBookmarksUseCase.observeForReference(bookmarkType, id),
                            userPreferencesRepository.userPreferencesFlow
                        ) { bookmark, prefs ->
                            Triple(detail, bookmark != null, prefs)
                        }
                    }
                }
                .collect { (detail, isBookmarked, prefs) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            song = detail,
                            isBookmarked = isBookmarked,
                            preferences = prefs
                        )
                    }
                }
        }
    }

    fun onIntent(intent: SongDetailIntent) {
        when (intent) {
            is SongDetailIntent.LoadSong -> {
                songIdFlow.value = intent.songId
            }

            is SongDetailIntent.ToggleBookmark -> {
                val song = _uiState.value.song ?: return
                val bType = if (song.type == SongType.CHORUS) BookmarkType.CHORUS else BookmarkType.HYMN
                viewModelScope.launch {
                    if (_uiState.value.isBookmarked) {
                        manageBookmarksUseCase.removeByReference(bType, song.id)
                        _effects.tryEmit(SongDetailEffect.ShowMessage("Eliminado de guardados"))
                    } else {
                        val preview = song.rawLyrics.lines().take(3).joinToString(" ").take(120)
                        manageBookmarksUseCase.save(
                            type = bType,
                            referenceId = song.id,
                            titlePreview = "#${song.number} · ${song.title}",
                            contentPreview = preview
                        )
                        _effects.tryEmit(SongDetailEffect.ShowMessage("Guardado en favoritos"))
                    }
                }
            }

            is SongDetailIntent.CopyLyrics -> {
                val song = _uiState.value.song ?: return
                val header = "#${song.number} - ${song.title}"
                _effects.tryEmit(SongDetailEffect.CopyToClipboard("$header\n\n${song.rawLyrics}", header))
                _effects.tryEmit(SongDetailEffect.ShowMessage("Letra copiada"))
            }

            is SongDetailIntent.UpdateFontSize -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateTypographySettings(fontSizeSp = intent.sizeSp)
                }
            }
        }
    }
}
