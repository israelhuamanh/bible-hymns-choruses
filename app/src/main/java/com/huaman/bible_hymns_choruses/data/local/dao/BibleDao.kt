package com.huaman.bible_hymns_choruses.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query(
        """
        SELECT * FROM verses
        WHERE book_id = :bookId AND chapter = :chapter
        ORDER BY verse ASC
        """
    )
    fun getChapterVerses(bookId: Int, chapter: Int): Flow<List<VerseEntity>>

    @Query(
        """
        SELECT v.id, v.book_id, b.name AS bookName, b.abbreviation AS bookAbbr, v.chapter, v.verse, v.text
        FROM verses v
        JOIN books b ON v.book_id = b.id
        WHERE v.book_id = :bookId
          AND v.chapter = :chapter
          AND (:startVerse IS NULL OR v.verse >= :startVerse)
          AND (:endVerse IS NULL OR v.verse <= :endVerse)
        ORDER BY v.verse ASC
        """
    )
    suspend fun getReferenceVerses(
        bookId: Int,
        chapter: Int,
        startVerse: Int? = null,
        endVerse: Int? = null
    ): List<VerseWithBookProjection>

    @Query(
        """
        SELECT v.id, v.book_id, b.name AS bookName, b.abbreviation AS bookAbbr, v.chapter, v.verse, v.text
        FROM verses v
        JOIN verses_fts fts ON v.id = fts.rowid
        JOIN books b ON v.book_id = b.id
        WHERE verses_fts MATCH :ftsQuery
        LIMIT 100
        """
    )
    suspend fun searchVersesFts(ftsQuery: String): List<VerseWithBookProjection>
}
