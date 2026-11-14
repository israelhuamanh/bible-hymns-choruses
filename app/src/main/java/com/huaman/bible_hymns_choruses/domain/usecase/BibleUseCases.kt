package com.huaman.bible_hymns_choruses.domain.usecase

import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.repository.BibleRepository
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBibleBooksUseCase @Inject constructor(
    private val bibleRepository: BibleRepository
) {
    operator fun invoke(): Flow<List<BibleBook>> =
        bibleRepository.books.map { list -> list.map { it.toDomain() } }
}

class GetBibleChapterUseCase @Inject constructor(
    private val bibleRepository: BibleRepository
) {
    operator fun invoke(bookId: Int, chapter: Int): Flow<List<BibleVerse>> =
        bibleRepository.getChapterVerses(bookId, chapter)
            .map { list -> list.map { it.toDomain() } }
}
