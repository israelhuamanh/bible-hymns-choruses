package com.huaman.bible_hymns_choruses.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongSummaryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query(
        """
        SELECT id, type, number, title, subtitle_en, tonality, category
        FROM songs
        WHERE type = :type
        ORDER BY number ASC
        """
    )
    fun getSongsByType(type: String): Flow<List<SongSummaryProjection>>

    @Query(
        """
        SELECT * FROM songs
        WHERE type = :type AND number = :number
        LIMIT 1
        """
    )
    suspend fun getSongByNumber(type: String, number: Int): SongEntity?

    @Query(
        """
        SELECT * FROM songs
        WHERE number = :number
        ORDER BY type ASC
        """
    )
    suspend fun getSongsByNumber(number: Int): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    fun getSongById(id: Int): Flow<SongEntity?>

    @Query(
        """
        SELECT s.* FROM songs s
        JOIN songs_fts fts ON s.id = fts.rowid
        WHERE songs_fts MATCH :ftsQuery
        ORDER BY s.type ASC, s.number ASC
        LIMIT 80
        """
    )
    suspend fun searchSongsFts(ftsQuery: String): List<SongEntity>
}
