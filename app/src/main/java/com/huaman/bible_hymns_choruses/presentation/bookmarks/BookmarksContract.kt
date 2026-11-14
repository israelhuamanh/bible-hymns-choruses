package com.huaman.bible_hymns_choruses.presentation.bookmarks

import androidx.compose.runtime.Immutable
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
    val allBookmarks: List<BookmarkItem> = emptyList(),
    val filteredBookmarks: List<BookmarkItem> = emptyList()
)

sealed interface BookmarksIntent {
    data class SelectFilter(val filter: BookmarkFilter) : BookmarksIntent
    data class DeleteBookmark(val id: Int) : BookmarksIntent
}

sealed interface BookmarksEffect {
    data class ShowMessage(val message: String) : BookmarksEffect
}
