package com.huaman.bible_hymns_choruses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.dao.NoteDao
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.local.entity.NoteEntity

/**
 * Dedicated persistent Room database for user bookmarks (versículos/himnos/coros guardados + notas)
 * and user study notes ("Apuntes" with text, images, and audio).
 * Kept separate from the prepackaged read-only content database (`bible_hymns_choruses_v3.db`)
 * so user data is never overwritten by asset reloads or content database upgrades.
 */
@Database(
    entities = [
        BookmarkEntity::class,
        NoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UserBookmarksDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "user_bookmarks.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `image_paths_json` TEXT NOT NULL,
                        `audio_paths_json` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
