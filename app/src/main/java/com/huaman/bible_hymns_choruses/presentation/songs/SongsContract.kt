package com.huaman.bible_hymns_choruses.presentation.songs

import androidx.compose.runtime.Immutable
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.domain.model.SongDetail
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType

@Immutable
data class SongsUiState(
    val isLoading: Boolean = true,
    val selectedType: SongType = SongType.HYMN,
    val jumpNumberQuery: String = "",
    val searchQuery: String = "",
    val selectedTonality: String? = null,
    val availableTonalities: List<String> = emptyList(),
    val allSongsForType: List<SongSummary> = emptyList(),
    val displayedSongs: List<SongSummary> = emptyList(),
    val isDeepFtsActive: Boolean = false
)

sealed interface SongsIntent {
    data class SelectType(val type: SongType) : SongsIntent
    data class UpdateJumpNumber(val numberText: String) : SongsIntent
    data object ExecuteJumpToNumber : SongsIntent
    data class UpdateSearchQuery(val query: String) : SongsIntent
    data class SelectTonality(val tonality: String?) : SongsIntent
}

sealed interface SongsEffect {
    data class NavigateToSongDetail(val songId: Int) : SongsEffect
    data class ShowMessage(val message: String) : SongsEffect
}

@Immutable
data class SongDetailUiState(
    val isLoading: Boolean = true,
    val song: SongDetail? = null,
    val isBookmarked: Boolean = false,
    val preferences: UserPreferences = UserPreferences()
)

sealed interface SongDetailIntent {
    data class LoadSong(val songId: Int) : SongDetailIntent
    data object ToggleBookmark : SongDetailIntent
    data object CopyLyrics : SongDetailIntent
    data class UpdateFontSize(val sizeSp: Float) : SongDetailIntent
}

sealed interface SongDetailEffect {
    data class CopyToClipboard(val text: String, val label: String) : SongDetailEffect
    data class ShowMessage(val message: String) : SongDetailEffect
}
