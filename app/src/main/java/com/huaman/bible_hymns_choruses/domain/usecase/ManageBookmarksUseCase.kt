package com.huaman.bible_hymns_choruses.domain.usecase

import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.repository.BookmarkRepository
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ManageBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    fun observeAll(): Flow<List<BookmarkItem>> =
        bookmarkRepository.getAllBookmarks().map { list -> list.map { it.toDomain() } }

    fun observeForReference(type: BookmarkType, referenceId: Int): Flow<BookmarkItem?> =
        bookmarkRepository.getBookmarkForReference(type.dbValue, referenceId)
            .map { it?.toDomain() }

    suspend fun save(
        type: BookmarkType,
        referenceId: Int,
        titlePreview: String,
        contentPreview: String,
        colorHex: String = "#D4AF37",
        note: String? = null
    ): Long {
        bookmarkRepository.deleteBookmarkByReference(type.dbValue, referenceId)
        val entity = BookmarkEntity(
            itemType = type.dbValue,
            referenceId = referenceId,
            titlePreview = titlePreview,
            contentPreview = contentPreview,
            colorHex = colorHex,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        return bookmarkRepository.saveBookmark(entity)
    }

    suspend fun removeById(id: Int) = bookmarkRepository.deleteBookmarkById(id)

    suspend fun removeByReference(type: BookmarkType, referenceId: Int) =
        bookmarkRepository.deleteBookmarkByReference(type.dbValue, referenceId)
}
