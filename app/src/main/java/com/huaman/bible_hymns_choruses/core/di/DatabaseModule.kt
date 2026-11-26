package com.huaman.bible_hymns_choruses.core.di

import android.content.Context
import androidx.room.Room
import com.huaman.bible_hymns_choruses.data.local.BibleHymnsDatabase
import com.huaman.bible_hymns_choruses.data.local.UserBookmarksDatabase
import com.huaman.bible_hymns_choruses.data.local.dao.BibleDao
import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.dao.NoteDao
import com.huaman.bible_hymns_choruses.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBibleHymnsDatabase(
        @ApplicationContext context: Context
    ): BibleHymnsDatabase {
        return Room.databaseBuilder(
            context,
            BibleHymnsDatabase::class.java,
            BibleHymnsDatabase.DATABASE_NAME
        )
            .createFromAsset(BibleHymnsDatabase.ASSET_DATABASE_PATH)
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserBookmarksDatabase(
        @ApplicationContext context: Context
    ): UserBookmarksDatabase {
        return Room.databaseBuilder(
            context,
            UserBookmarksDatabase::class.java,
            UserBookmarksDatabase.DATABASE_NAME
        )
            .addMigrations(UserBookmarksDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideBibleDao(database: BibleHymnsDatabase): BibleDao = database.bibleDao()

    @Provides
    fun provideSongDao(database: BibleHymnsDatabase): SongDao = database.songDao()

    @Provides
    fun provideBookmarkDao(database: UserBookmarksDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideNoteDao(database: UserBookmarksDatabase): NoteDao = database.noteDao()
}
