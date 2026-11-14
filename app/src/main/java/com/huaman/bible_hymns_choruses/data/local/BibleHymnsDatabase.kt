package com.huaman.bible_hymns_choruses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huaman.bible_hymns_choruses.data.local.dao.BibleDao
import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.dao.SongDao
import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongFtsEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseFtsEntity

@Database(
    entities = [
        BookEntity::class,
        VerseEntity::class,
        VerseFtsEntity::class,
        SongEntity::class,
        SongFtsEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BibleHymnsDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao
    abstract fun songDao(): SongDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "bible_hymns_choruses.db"
        const val ASSET_DATABASE_PATH = "database/bible_hymns_choruses.db"
    }
}
