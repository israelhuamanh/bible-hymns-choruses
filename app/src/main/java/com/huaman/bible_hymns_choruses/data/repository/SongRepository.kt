package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.dao.SongDao
import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongSummaryProjection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao
) {
    fun getSongsByType(type: String): Flow<List<SongSummaryProjection>> {
        return songDao.getSongsByType(type).flowOn(Dispatchers.IO)
    }

    fun getSongById(id: Int): Flow<SongEntity?> {
        return songDao.getSongById(id).flowOn(Dispatchers.IO)
    }

    suspend fun getSongByNumber(type: String, number: Int): SongEntity? =
        withContext(Dispatchers.IO) {
            songDao.getSongByNumber(type, number)
        }

    suspend fun getSongsByNumber(number: Int): List<SongEntity> =
        withContext(Dispatchers.IO) {
            songDao.getSongsByNumber(number)
        }

    suspend fun searchSongsFts(rawQuery: String): List<SongEntity> =
        withContext(Dispatchers.IO) {
            val ftsQuery = SmartQueryRouter.buildFtsPrefixQuery(rawQuery)
            if (ftsQuery.isBlank()) return@withContext emptyList()
            songDao.searchSongsFts(ftsQuery)
        }
}
