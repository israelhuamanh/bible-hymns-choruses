package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class SmartSearchResult(
    val route: QueryRoute,
    val directSongs: List<SongEntity> = emptyList(),
    val directVerses: List<VerseWithBookProjection> = emptyList(),
    val ftsVerses: List<VerseWithBookProjection> = emptyList(),
    val ftsSongs: List<SongEntity> = emptyList()
)

@Singleton
class UnifiedSearchRepository @Inject constructor(
    private val bibleRepository: BibleRepository,
    private val songRepository: SongRepository
) {
    /**
     * Executes the Smart Query Router on [Dispatchers.IO]:
     * - Pure number -> direct `idx_songs_type_number` lookup (< 1 ms) + FTS4 matches if applicable.
     * - Bible reference -> direct `idx_verses_location` lookup without hitting FTS4.
     * - Free text -> parallel FTS4 `MATCH` queries on `verses_fts` and `songs_fts`.
     */
    suspend fun search(
        rawQuery: String,
        versionId: String = "RVR1960"
    ): SmartSearchResult = withContext(Dispatchers.IO) {
        val books = bibleRepository.books.value
        val route = SmartQueryRouter.route(rawQuery, books)

        when (route) {
            is QueryRoute.Empty -> SmartSearchResult(route = route)

            is QueryRoute.PureNumber -> {
                val directSongsDeferred = async { songRepository.getSongsByNumber(route.number) }
                val ftsVersesDeferred = async { bibleRepository.searchVersesFts(versionId, rawQuery) }
                SmartSearchResult(
                    route = route,
                    directSongs = directSongsDeferred.await(),
                    ftsVerses = ftsVersesDeferred.await()
                )
            }

            is QueryRoute.BibleReference -> {
                val verses = bibleRepository.getReferenceVerses(
                    versionId = versionId,
                    bookId = route.book.id,
                    chapter = route.chapter,
                    startVerse = route.startVerse,
                    endVerse = route.endVerse
                )
                SmartSearchResult(
                    route = route,
                    directVerses = verses
                )
            }

            is QueryRoute.FullText -> {
                val ftsVersesDeferred = async {
                    bibleRepository.searchVersesFts(versionId, route.normalizedQuery)
                }
                val ftsSongsDeferred = async { songRepository.searchSongsFts(route.normalizedQuery) }
                SmartSearchResult(
                    route = route,
                    ftsVerses = ftsVersesDeferred.await(),
                    ftsSongs = ftsSongsDeferred.await()
                )
            }
        }
    }
}
