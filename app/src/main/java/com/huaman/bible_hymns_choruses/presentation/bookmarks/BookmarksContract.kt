package com.huaman.bible_hymns_choruses.presentation.bookmarks

import androidx.compose.runtime.Immutable
import com.huaman.bible_hymns_choruses.core.util.DateSortOrder
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType

@Immutable
enum class BookmarkFilter(val label: String, val type: BookmarkType?) {
    ALL("Todos", null),
    VERSES("Versículos", BookmarkType.VERSE),
    HYMNS("Himnos", BookmarkType.HYMN),
    CHORUSES("Coros", BookmarkType.CHORUS)
}

@Immutable
data class BookmarksUiState(
    val isLoading: Boolean = true,
    val selectedFilter: BookmarkFilter = BookmarkFilter.ALL,
    val sortOrder: DateSortOrder = DateSortOrder.NEWEST_FIRST,
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val availableMonths: List<Int> = emptyList(),
    val selectedMonth: Int? = null,
    val allBookmarks: List<BookmarkItem> = emptyList(),
    val filteredBookmarks: List<BookmarkItem> = emptyList()
)

sealed interface BookmarksIntent {
    data class SelectFilter(val filter: BookmarkFilter) : BookmarksIntent
    data object ToggleSortOrder : BookmarksIntent
    data class SelectYear(val year: Int?) : BookmarksIntent
    data class SelectMonth(val month: Int?) : BookmarksIntent
    data class UpdateBookmarkNote(val id: Int, val note: String) : BookmarksIntent
    data class DeleteBookmark(val id: Int) : BookmarksIntent
    data object UndoDeleteBookmark : BookmarksIntent
    data class OpenVerseInBible(val referenceId: Int) : BookmarksIntent
}

sealed interface BookmarksEffect {
    data class ShowMessage(val message: String) : BookmarksEffect
    data class ShowUndoDelete(val message: String) : BookmarksEffect
    data class NavigateToBiblePassage(
        val bookId: Int,
        val chapter: Int,
        val verse: Int
    ) : BookmarksEffect
    data object NoteUpdated : BookmarksEffect
}
