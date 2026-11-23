package com.huaman.bible_hymns_choruses.presentation.reader

import androidx.compose.runtime.Immutable
import com.huaman.bible_hymns_choruses.core.datastore.ThemeMode
import com.huaman.bible_hymns_choruses.core.datastore.UserPreferences
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import com.huaman.bible_hymns_choruses.domain.model.BibleVersion
import com.huaman.bible_hymns_choruses.domain.model.Testament

@Immutable
data class BibleReaderUiState(
    val isLoading: Boolean = true,
    val versions: List<BibleVersion> = emptyList(),
    val currentVersionId: String = UserPreferences.DEFAULT_BIBLE_VERSION_ID,
    val books: List<BibleBook> = emptyList(),
    val currentBook: BibleBook? = null,
    val currentChapter: Int = 1,
    val verses: List<BibleVerse> = emptyList(),
    val selectedVerseIds: Set<Int> = emptySet(),
    val highlightedVerseNumber: Int? = null,
    val bookmarkedVerseIds: Set<Int> = emptySet(),
    val isBookPickerVisible: Boolean = false,
    val isVersionPickerVisible: Boolean = false,
    val pickerTestament: Testament = Testament.AT,
    val isTypographySheetVisible: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val hasPreviousChapter: Boolean = false,
    val hasNextChapter: Boolean = true
)

sealed interface BibleReaderIntent {
    data class SelectLocation(
        val bookId: Int,
        val chapter: Int,
        val highlightVerse: Int? = null
    ) : BibleReaderIntent

    data class ToggleVersionPicker(val visible: Boolean) : BibleReaderIntent
    data class SelectVersion(val versionId: String) : BibleReaderIntent

    data object PreviousChapter : BibleReaderIntent
    data object NextChapter : BibleReaderIntent
    data class ToggleVerseSelection(val verseId: Int) : BibleReaderIntent
    data object ClearSelection : BibleReaderIntent
    data object CopySelectedVerses : BibleReaderIntent
    data class BookmarkSelectedVerses(val colorHex: String = "#D4AF37") : BibleReaderIntent
    data class ToggleBookPicker(val visible: Boolean) : BibleReaderIntent
    data class SelectPickerTestament(val testament: Testament) : BibleReaderIntent
    data class ToggleTypographySheet(val visible: Boolean) : BibleReaderIntent
    data class UpdateFontSize(val sizeSp: Float) : BibleReaderIntent
    data class UpdateFontWeight(val weight: Int) : BibleReaderIntent
    data class UpdateFontGrade(val grade: Int) : BibleReaderIntent
    data class UpdateThemeMode(val themeMode: ThemeMode) : BibleReaderIntent
}

sealed interface BibleReaderEffect {
    data class CopyToClipboard(val text: String, val label: String) : BibleReaderEffect
    data class ShowMessage(val message: String) : BibleReaderEffect
    data class ScrollToVerse(val verseNumber: Int) : BibleReaderEffect
}
