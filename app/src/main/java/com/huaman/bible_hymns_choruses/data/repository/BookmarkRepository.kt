package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.dao.BibleDao
import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val bibleDao: BibleDao
) {
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks().flowOn(Dispatchers.IO)
    }

    fun getBookmarksByType(itemType: String): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getBookmarksByType(itemType).flowOn(Dispatchers.IO)
    }

    fun getBookmarkForReference(itemType: String, referenceId: Int): Flow<BookmarkEntity?> {
        return bookmarkDao.getBookmarkForReference(itemType, referenceId).flowOn(Dispatchers.IO)
    }

    suspend fun getBookmarkById(id: Int): BookmarkEntity? = withContext(Dispatchers.IO) {
        bookmarkDao.getBookmarkById(id)
    }

    suspend fun saveBookmark(bookmark: BookmarkEntity): Long = withContext(Dispatchers.IO) {
        bookmarkDao.upsertBookmark(bookmark)
    }

    suspend fun updateBookmarkNote(id: Int, note: String?) = withContext(Dispatchers.IO) {
        bookmarkDao.updateBookmarkNote(id, note)
    }

    suspend fun deleteBookmarkById(id: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun deleteBookmarkByReference(itemType: String, referenceId: Int) =
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkByReference(itemType, referenceId)
        }

    suspend fun getVerseById(verseId: Int): VerseEntity? = withContext(Dispatchers.IO) {
        bibleDao.getVerseById(verseId)
    }
}

