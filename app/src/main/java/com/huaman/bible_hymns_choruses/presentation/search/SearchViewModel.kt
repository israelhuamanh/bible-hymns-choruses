package com.huaman.bible_hymns_choruses.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferencesRepository
import com.huaman.bible_hymns_choruses.domain.model.UnifiedSearchResult
import com.huaman.bible_hymns_choruses.domain.usecase.SmartSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val smartSearchUseCase: SmartSearchUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        observeReactiveSearch()
    }

    private fun observeReactiveSearch() {
        viewModelScope.launch {
            val activeVersionFlow = userPreferencesRepository.userPreferencesFlow
                .map { it.selectedBibleVersionId }
                .distinctUntilChanged()

            combine(
                queryFlow.debounce(150L).distinctUntilChanged(),
                activeVersionFlow
            ) { q, versionId -> q to versionId }
                .flatMapLatest { (q, versionId) ->
                    flow {
                        _uiState.update { it.copy(activeVersionId = versionId) }
                        if (q.isBlank()) {
                            emit(UnifiedSearchResult(query = ""))
                        } else {
                            _uiState.update { it.copy(isSearching = true) }
                            emit(smartSearchUseCase(q, versionId))
                        }
                    }.flowOn(Dispatchers.IO)
                }
                .collect { searchResult ->
                    _uiState.update { state ->
                        state.copy(
                            isSearching = false,
                            result = searchResult
                        )
                    }
                }
        }
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                queryFlow.value = intent.query
                _uiState.update {
                    it.copy(
                        query = intent.query,
                        isSearching = intent.query.isNotBlank()
                    )
                }
            }

            is SearchIntent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = intent.tab) }
            }

            is SearchIntent.ClearQuery -> {
                queryFlow.value = ""
                _uiState.update {
                    it.copy(
                        query = "",
                        isSearching = false,
                        result = UnifiedSearchResult(query = "")
                    )
                }
            }
        }
    }
}
