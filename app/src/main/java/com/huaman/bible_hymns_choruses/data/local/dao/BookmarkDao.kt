package com.huaman.bible_hymns_choruses.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE item_type = :itemType ORDER BY created_at DESC")
    fun getBookmarksByType(itemType: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE item_type = :itemType AND reference_id = :referenceId LIMIT 1")
    fun getBookmarkForReference(itemType: String, referenceId: Int): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkById(id: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity): Long

    @Query("UPDATE bookmarks SET note = :note WHERE id = :id")
    suspend fun updateBookmarkNote(id: Int, note: String?)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    @Query("DELETE FROM bookmarks WHERE item_type = :itemType AND reference_id = :referenceId")
    suspend fun deleteBookmarkByReference(itemType: String, referenceId: Int)
}
