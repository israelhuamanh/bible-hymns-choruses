package com.huaman.bible_hymns_choruses.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.huaman.bible_hymns_choruses.data.local.entity.BibleVersionEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    @Query("SELECT * FROM bible_versions ORDER BY sort_order ASC")
    fun getAllVersions(): Flow<List<BibleVersionEntity>>

    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query(
        """
        SELECT * FROM verses
        WHERE version_id = :versionId AND book_id = :bookId AND chapter = :chapter
        ORDER BY item_order ASC
        """
    )
    fun getChapterVerses(versionId: String, bookId: Int, chapter: Int): Flow<List<VerseEntity>>

    @Query(
        """
        SELECT v.id, v.version_id, v.book_id, b.name AS bookName, b.abbreviation AS bookAbbr,
               v.chapter, v.verse, v.verse_end, v.verse_label, v.text
        FROM verses v
        JOIN books b ON v.book_id = b.id
        WHERE v.version_id = :versionId
          AND v.book_id = :bookId
          AND v.chapter = :chapter
          AND (:startVerse IS NULL OR v.verse_end >= :startVerse)
          AND (:endVerse IS NULL OR v.verse <= :endVerse)
        ORDER BY v.item_order ASC
        """
    )
    suspend fun getReferenceVerses(
        versionId: String,
        bookId: Int,
        chapter: Int,
        startVerse: Int? = null,
        endVerse: Int? = null
    ): List<VerseWithBookProjection>

    @Query(
        """
        SELECT v.id, v.version_id, v.book_id, b.name AS bookName, b.abbreviation AS bookAbbr,
               v.chapter, v.verse, v.verse_end, v.verse_label, v.text
        FROM verses_fts fts
        CROSS JOIN verses v ON v.id = fts.rowid
        JOIN books b ON v.book_id = b.id
        WHERE verses_fts MATCH :ftsQuery
          AND v.version_id = :versionId
        LIMIT 100
        """
    )
    suspend fun searchVersesFts(versionId: String, ftsQuery: String): List<VerseWithBookProjection>
}
