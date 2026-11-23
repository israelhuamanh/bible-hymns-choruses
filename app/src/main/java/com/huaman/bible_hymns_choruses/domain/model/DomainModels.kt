package com.huaman.bible_hymns_choruses.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class Testament {
    AT,
    NT
}

@Immutable
data class BibleVersion(
    val id: String,
    val name: String,
    val fullTitle: String,
    val copyright: String,
    val sortOrder: Int
)

@Immutable
data class BibleBook(
    val id: Int,
    val testament: Testament,
    val name: String,
    val abbreviation: String,
    val chapterCount: Int
)

@Immutable
data class BibleVerse(
    val id: Int,
    val versionId: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val verseEnd: Int = verse,
    val verseLabel: String = verse.toString(),
    val heading: String? = null,
    val label: String? = null,
    val text: String
)

@Immutable
data class VerseSearchMatch(
    val id: Int,
    val versionId: String,
    val bookId: Int,
    val bookName: String,
    val bookAbbr: String,
    val chapter: Int,
    val verse: Int,
    val verseEnd: Int = verse,
    val verseLabel: String = verse.toString(),
    val text: String
)

@Immutable
enum class SongType(val dbValue: String, val label: String) {
    HYMN("HYMN", "Himnos"),
    CHORUS("CHORUS", "Coros");

    companion object {
        fun fromDb(value: String): SongType =
            entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) } ?: HYMN
    }
}

@Immutable
data class SongSummary(
    val id: Int,
    val type: SongType,
    val number: Int,
    val title: String,
    val subtitleEn: String?,
    val tonality: String?,
    val category: String?
)

@Immutable
data class SongStanzaBlock(
    val label: String, // "1", "2", "CORO", etc.
    val isChorus: Boolean,
    val lines: List<String>
)

@Immutable
data class SongDetail(
    val id: Int,
    val type: SongType,
    val number: Int,
    val title: String,
    val subtitleEn: String?,
    val tonality: String?,
    val category: String?,
    val rawLyrics: String,
    val blocks: List<SongStanzaBlock>
)

@Immutable
enum class BookmarkType(val dbValue: String, val label: String) {
    VERSE("VERSE", "Versículos"),
    HYMN("HYMN", "Himnos"),
    CHORUS("CHORUS", "Coros");

    companion object {
        fun fromDb(value: String): BookmarkType =
            entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) } ?: VERSE
    }
}

@Immutable
data class BookmarkItem(
    val id: Int,
    val itemType: BookmarkType,
    val referenceId: Int,
    val titlePreview: String,
    val contentPreview: String,
    val colorHex: String,
    val note: String?,
    val createdAt: Long
)

@Immutable
sealed interface DirectShortcut {
    data class BiblePassage(
        val bookId: Int,
        val bookName: String,
        val bookAbbr: String,
        val chapter: Int,
        val startVerse: Int?,
        val endVerse: Int?,
        val previewText: String
    ) : DirectShortcut

    data class SongMatch(
        val song: SongDetail
    ) : DirectShortcut
}

@Immutable
data class UnifiedSearchResult(
    val query: String,
    val shortcuts: List<DirectShortcut> = emptyList(),
    val bibleMatches: List<VerseSearchMatch> = emptyList(),
    val hymnMatches: List<SongSummary> = emptyList(),
    val chorusMatches: List<SongSummary> = emptyList()
)
