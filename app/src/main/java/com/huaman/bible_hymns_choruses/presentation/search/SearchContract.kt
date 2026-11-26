package com.huaman.bible_hymns_choruses.presentation.search

import androidx.compose.runtime.Immutable
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.domain.model.UnifiedSearchResult

@Immutable
enum class SearchTab(val label: String) {
    ALL("Todo"),
    BIBLE("Biblia"),
    HYMNS("Himnos"),
    CHORUSES("Coros")
}

@Immutable
data class SearchUiState(
    val query: String = "",
    val activeVersionId: String = UserPreferences.DEFAULT_BIBLE_VERSION_ID,
    val selectedTab: SearchTab = SearchTab.ALL,
    val isSearching: Boolean = false,
    val result: UnifiedSearchResult = UnifiedSearchResult(query = ""),
    val recentQueries: List<String> = listOf("Juan 3:16", "Salmo 23", "104", "Gracia", "Amor")
)

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data class SelectTab(val tab: SearchTab) : SearchIntent
    data object ClearQuery : SearchIntent
}

sealed interface SearchEffect
