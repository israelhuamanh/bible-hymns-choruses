package com.huaman.bible_hymns_choruses.presentation.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.data.repository.SmartQueryRouter
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.usecase.GetSongDetailUseCase
import com.huaman.bible_hymns_choruses.domain.usecase.GetSongsCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SongsViewModel @Inject constructor(
    private val getSongsCatalogUseCase: GetSongsCatalogUseCase,
    private val getSongDetailUseCase: GetSongDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SongsEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<SongsEffect> = _effects.asSharedFlow()

    private val selectedTypeFlow = MutableStateFlow(SongType.HYMN)
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedTonalityFlow = MutableStateFlow<String?>(null)

    init {
        observeCatalogAndFilters()
    }

    private fun observeCatalogAndFilters() {
        val catalogFlow = selectedTypeFlow
            .flatMapLatest { type -> getSongsCatalogUseCase(type) }

        val ftsMatchesFlow = searchQueryFlow
            .debounce(150L)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    val trimmed = query.trim()
                    if (trimmed.length >= 2 && trimmed.toIntOrNull() == null) {
                        emit(getSongDetailUseCase.searchLyricsFts(trimmed))
                    } else {
                        emit(emptyList())
                    }
                }.flowOn(Dispatchers.IO)
            }

        viewModelScope.launch {
            combine(
                selectedTypeFlow,
                catalogFlow,
                searchQueryFlow,
                selectedTonalityFlow,
                ftsMatchesFlow
            ) { type, allSongs, rawQuery, tonality, ftsMatches ->
                val tonalities = allSongs
                    .mapNotNull { it.tonality }
                    .distinct()
                    .sorted()

                val filtered = filterInMemoryAndMergeFts(
                    type = type,
                    allSongs = allSongs,
                    rawQuery = rawQuery,
                    tonality = tonality,
                    ftsMatches = ftsMatches
                )

                SongsUiState(
                    isLoading = false,
                    selectedType = type,
                    jumpNumberQuery = _uiState.value.jumpNumberQuery,
                    searchQuery = rawQuery,
                    selectedTonality = tonality,
                    availableTonalities = tonalities,
                    allSongsForType = allSongs,
                    displayedSongs = filtered,
                    isDeepFtsActive = rawQuery.trim().length >= 2 && rawQuery.trim().toIntOrNull() == null
                )
            }
                .flowOn(Dispatchers.Default)
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    private fun filterInMemoryAndMergeFts(
        type: SongType,
        allSongs: List<SongSummary>,
        rawQuery: String,
        tonality: String?,
        ftsMatches: List<SongSummary>
    ): List<SongSummary> {
        val trimmed = rawQuery.trim()
        val normalizedQuery = SmartQueryRouter.normalizeText(trimmed)
        val numberQuery = trimmed.removePrefix("#").toIntOrNull()

        val baseList = if (tonality != null) {
            allSongs.filter { it.tonality == tonality }
        } else {
            allSongs
        }

        if (trimmed.isEmpty()) return baseList

        // 1. Instant in-memory filter by number or title
        val memoryMatches = baseList.filter { song ->
            if (numberQuery != null) {
                song.number == numberQuery || song.number.toString().startsWith(numberQuery.toString())
            } else {
                SmartQueryRouter.normalizeText(song.title).contains(normalizedQuery) ||
                    (song.subtitleEn?.let { SmartQueryRouter.normalizeText(it).contains(normalizedQuery) } == true)
            }
        }

        // 2. Merge deep FTS4 lyrics matches for the active SongType
        val ftsFiltered = ftsMatches.filter { song ->
            song.type == type && (tonality == null || song.tonality == tonality)
        }

        return (memoryMatches + ftsFiltered).distinctBy { it.id }
    }

    fun onIntent(intent: SongsIntent) {
        when (intent) {
            is SongsIntent.SelectType -> {
                selectedTypeFlow.value = intent.type
                selectedTonalityFlow.value = null
                _uiState.update { it.copy(selectedType = intent.type, selectedTonality = null) }
            }

            is SongsIntent.UpdateJumpNumber -> {
                val digitsOnly = intent.numberText.filter { it.isDigit() }.take(4)
                _uiState.update { it.copy(jumpNumberQuery = digitsOnly) }
            }

            is SongsIntent.ExecuteJumpToNumber -> {
                val num = _uiState.value.jumpNumberQuery.toIntOrNull()
                if (num == null) {
                    _effects.tryEmit(SongsEffect.ShowMessage("Ingresa un número válido"))
                    return
                }
                val type = _uiState.value.selectedType
                viewModelScope.launch {
                    val song = getSongDetailUseCase.byNumber(type, num)
                    if (song != null) {
                        _uiState.update { it.copy(jumpNumberQuery = "") }
                        _effects.tryEmit(SongsEffect.NavigateToSongDetail(song.id))
                    } else {
                        _effects.tryEmit(
                            SongsEffect.ShowMessage("No se encontró ${type.label.dropLast(1)} #$num")
                        )
                    }
                }
            }

            is SongsIntent.UpdateSearchQuery -> {
                searchQueryFlow.value = intent.query
                _uiState.update { it.copy(searchQuery = intent.query) }
            }

            is SongsIntent.SelectTonality -> {
                val next = if (_uiState.value.selectedTonality == intent.tonality) null else intent.tonality
                selectedTonalityFlow.value = next
                _uiState.update { it.copy(selectedTonality = next) }
            }
        }
    }
}
