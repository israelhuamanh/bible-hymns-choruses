package com.huaman.bible_hymns_choruses.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaman.bible_hymns_choruses.core.util.DateSortOrder
import com.huaman.bible_hymns_choruses.core.util.extractMonth
import com.huaman.bible_hymns_choruses.core.util.extractYear
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.usecase.ManageBookmarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val manageBookmarksUseCase: ManageBookmarksUseCase
) : ViewModel() {

    private data class FilterCriteria(
        val filter: BookmarkFilter = BookmarkFilter.ALL,
        val sortOrder: DateSortOrder = DateSortOrder.NEWEST_FIRST,
        val selectedYear: Int? = null,
        val selectedMonth: Int? = null
    )

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<BookmarksEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<BookmarksEffect> = _effects.asSharedFlow()

    private val criteriaFlow = MutableStateFlow(FilterCriteria())
    private var lastDeletedBookmark: BookmarkItem? = null

    init {
        viewModelScope.launch {
            combine(
                manageBookmarksUseCase.observeAll(),
                criteriaFlow
            ) { all, criteria ->
                val byType = if (criteria.filter.type == null) {
                    all
                } else {
                    all.filter { it.itemType == criteria.filter.type }
                }

                val years = byType
                    .map { extractYear(it.createdAt) }
                    .distinct()
                    .sortedDescending()

                val activeYear = criteria.selectedYear?.takeIf { it in years }

                val byYear = if (activeYear == null) {
                    byType
                } else {
                    byType.filter { extractYear(it.createdAt) == activeYear }
                }

                val months = byYear
                    .map { extractMonth(it.createdAt) }
                    .distinct()
                    .sorted()

                val activeMonth = criteria.selectedMonth?.takeIf { it in months }

                val byMonth = if (activeMonth == null) {
                    byYear
                } else {
                    byYear.filter { extractMonth(it.createdAt) == activeMonth }
                }

                val sorted = when (criteria.sortOrder) {
                    DateSortOrder.NEWEST_FIRST -> byMonth.sortedByDescending { it.createdAt }
                    DateSortOrder.OLDEST_FIRST -> byMonth.sortedBy { it.createdAt }
                }

                BookmarksUiState(
                    isLoading = false,
                    selectedFilter = criteria.filter,
                    sortOrder = criteria.sortOrder,
                    availableYears = years,
                    selectedYear = activeYear,
                    availableMonths = months,
                    selectedMonth = activeMonth,
                    allBookmarks = all,
                    filteredBookmarks = sorted
                )
            }
                .flowOn(Dispatchers.Default)
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun onIntent(intent: BookmarksIntent) {
        when (intent) {
            is BookmarksIntent.SelectFilter -> {
                criteriaFlow.value = criteriaFlow.value.copy(filter = intent.filter)
            }

            is BookmarksIntent.ToggleSortOrder -> {
                val next = if (criteriaFlow.value.sortOrder == DateSortOrder.NEWEST_FIRST) {
                    DateSortOrder.OLDEST_FIRST
                } else {
                    DateSortOrder.NEWEST_FIRST
                }
                criteriaFlow.value = criteriaFlow.value.copy(sortOrder = next)
            }

            is BookmarksIntent.SelectYear -> {
                criteriaFlow.value = criteriaFlow.value.copy(
                    selectedYear = intent.year,
                    selectedMonth = null
                )
            }

            is BookmarksIntent.SelectMonth -> {
                criteriaFlow.value = criteriaFlow.value.copy(selectedMonth = intent.month)
            }

            is BookmarksIntent.UpdateBookmarkNote -> {
                viewModelScope.launch {
                    manageBookmarksUseCase.updateNote(intent.id, intent.note)
                    _effects.tryEmit(BookmarksEffect.ShowMessage("Nota personal actualizada"))
                    _effects.tryEmit(BookmarksEffect.NoteUpdated)
                }
            }

            is BookmarksIntent.DeleteBookmark -> {
                val target = _uiState.value.allBookmarks.firstOrNull { it.id == intent.id }
                viewModelScope.launch {
                    lastDeletedBookmark = target
                    manageBookmarksUseCase.removeById(intent.id)
                    _effects.tryEmit(BookmarksEffect.ShowUndoDelete("Guardado eliminado"))
                }
            }

            is BookmarksIntent.UndoDeleteBookmark -> {
                val deleted = lastDeletedBookmark ?: return
                lastDeletedBookmark = null
                viewModelScope.launch {
                    manageBookmarksUseCase.restore(deleted)
                    _effects.tryEmit(BookmarksEffect.ShowMessage("Guardado restaurado"))
                }
            }

            is BookmarksIntent.OpenVerseInBible -> {
                viewModelScope.launch {
                    val loc = manageBookmarksUseCase.resolveVerseLocation(intent.referenceId)
                    if (loc != null) {
                        _effects.tryEmit(
                            BookmarksEffect.NavigateToBiblePassage(
                                bookId = loc.first,
                                chapter = loc.second,
                                verse = loc.third
                            )
                        )
                    } else {
                        _effects.tryEmit(
                            BookmarksEffect.ShowMessage("No se pudo abrir el capítulo")
                        )
                    }
                }
            }
        }
    }
}
