package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.dao.BibleDao
import com.huaman.bible_hymns_choruses.data.local.entity.BibleVersionEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

@Singleton
class BibleRepository @Inject constructor(
    private val bibleDao: BibleDao
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * All offline Bible versions (RVR1960, RVC, TLA) cached in memory for 0 ms lookup.
     */
    val versions: StateFlow<List<BibleVersionEntity>> = bibleDao.getAllVersions()
        .stateIn(
            scope = repoScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * All 66 canonical books cached in memory with `stateIn` for 0 ms lookup.
     */
    val books: StateFlow<List<BookEntity>> = bibleDao.getAllBooks()
        .stateIn(
            scope = repoScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun getChapterVerses(versionId: String, bookId: Int, chapter: Int): Flow<List<VerseEntity>> {
        return bibleDao.getChapterVerses(versionId, bookId, chapter)
            .flowOn(Dispatchers.IO)
    }

    suspend fun getReferenceVerses(
        versionId: String,
        bookId: Int,
        chapter: Int,
        startVerse: Int? = null,
        endVerse: Int? = null
    ): List<VerseWithBookProjection> = withContext(Dispatchers.IO) {
        bibleDao.getReferenceVerses(versionId, bookId, chapter, startVerse, endVerse)
    }

    suspend fun searchVersesFts(versionId: String, rawQuery: String): List<VerseWithBookProjection> =
        withContext(Dispatchers.IO) {
            val ftsQuery = SmartQueryRouter.buildFtsPrefixQuery(rawQuery)
            if (ftsQuery.isBlank()) return@withContext emptyList()
            bibleDao.searchVersesFts(versionId, ftsQuery)
        }
}
