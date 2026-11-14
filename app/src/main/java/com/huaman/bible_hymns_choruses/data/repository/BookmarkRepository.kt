package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.dao.BookmarkDao
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
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

    suspend fun saveBookmark(bookmark: BookmarkEntity): Long = withContext(Dispatchers.IO) {
        bookmarkDao.upsertBookmark(bookmark)
    }

    suspend fun deleteBookmarkById(id: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun deleteBookmarkByReference(itemType: String, referenceId: Int) =
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkByReference(itemType, referenceId)
        }
}
