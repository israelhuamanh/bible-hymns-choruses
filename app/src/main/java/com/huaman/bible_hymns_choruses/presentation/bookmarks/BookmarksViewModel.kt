package com.huaman.bible_hymns_choruses.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.domain.usecase.ManageBookmarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val manageBookmarksUseCase: ManageBookmarksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<BookmarksEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<BookmarksEffect> = _effects.asSharedFlow()

    private val selectedFilterFlow = MutableStateFlow(BookmarkFilter.ALL)

    init {
        viewModelScope.launch {
            combine(
                manageBookmarksUseCase.observeAll(),
                selectedFilterFlow
            ) { all, filter ->
                val filtered = if (filter.type == null) {
                    all
                } else {
                    all.filter { it.itemType == filter.type }
                }
                BookmarksUiState(
                    isLoading = false,
                    selectedFilter = filter,
                    allBookmarks = all,
                    filteredBookmarks = filtered
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onIntent(intent: BookmarksIntent) {
        when (intent) {
            is BookmarksIntent.SelectFilter -> {
                selectedFilterFlow.value = intent.filter
            }

            is BookmarksIntent.DeleteBookmark -> {
                viewModelScope.launch {
                    manageBookmarksUseCase.removeById(intent.id)
                    _effects.tryEmit(BookmarksEffect.ShowMessage("Guardado eliminado"))
                }
            }
        }
    }
}
