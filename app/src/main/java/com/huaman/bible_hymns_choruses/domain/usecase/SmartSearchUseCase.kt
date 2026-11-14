package com.huaman.bible_hymns_choruses.domain.usecase

import com.huaman.bible_hymns_choruses.data.mapper.toDetailDomain
import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.mapper.toSummaryDomain
import com.huaman.bible_hymns_choruses.data.repository.QueryRoute
import com.huaman.bible_hymns_choruses.data.repository.UnifiedSearchRepository
import com.huaman.bible_hymns_choruses.domain.model.DirectShortcut
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.model.UnifiedSearchResult
import javax.inject.Inject

class SmartSearchUseCase @Inject constructor(
    private val unifiedSearchRepository: UnifiedSearchRepository
) {
    suspend operator fun invoke(rawQuery: String): UnifiedSearchResult {
        val rawResult = unifiedSearchRepository.search(rawQuery)
        val shortcuts = mutableListOf<DirectShortcut>()

        when (val route = rawResult.route) {
            is QueryRoute.PureNumber -> {
                rawResult.directSongs.forEach { song ->
                    shortcuts.add(DirectShortcut.SongMatch(song.toDetailDomain()))
                }
            }
            is QueryRoute.BibleReference -> {
                val firstVerseText = rawResult.directVerses.firstOrNull()?.text.orEmpty()
                shortcuts.add(
                    DirectShortcut.BiblePassage(
                        bookId = route.book.id,
                        bookName = route.book.name,
                        bookAbbr = route.book.abbreviation,
                        chapter = route.chapter,
                        startVerse = route.startVerse,
                        endVerse = route.endVerse,
                        previewText = firstVerseText
                    )
                )
            }
            is QueryRoute.Empty, is QueryRoute.FullText -> Unit
        }

        val allVerses = (rawResult.directVerses + rawResult.ftsVerses)
            .distinctBy { it.id }
            .map { it.toDomain() }

        val hymns = rawResult.ftsSongs
            .filter { SongType.fromDb(it.type) == SongType.HYMN }
            .map { it.toSummaryDomain() }

        val choruses = rawResult.ftsSongs
            .filter { SongType.fromDb(it.type) == SongType.CHORUS }
            .map { it.toSummaryDomain() }

        return UnifiedSearchResult(
            query = rawQuery,
            shortcuts = shortcuts,
            bibleMatches = allVerses,
            hymnMatches = hymns,
            chorusMatches = choruses
        )
    }
}
