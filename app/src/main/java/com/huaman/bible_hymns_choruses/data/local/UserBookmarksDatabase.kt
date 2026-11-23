package com.huaman.bible_hymns_choruses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity

/**
 * Dedicated persistent Room database for user bookmarks.
 * Kept separate from the prepackaged read-only content database (`bible_hymns_choruses.db`)
 * so user data is never overwritten by asset reloads or content database upgrades.
 */
@Database(
    entities = [BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UserBookmarksDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "user_bookmarks.db"
    }
}
