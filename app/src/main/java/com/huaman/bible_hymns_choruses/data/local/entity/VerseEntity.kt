package com.huaman.bible_hymns_choruses.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verses",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["book_id", "chapter", "verse"], unique = true, name = "idx_verses_location")
    ]
)
data class VerseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    @ColumnInfo(name = "chapter")
    val chapter: Int,
    @ColumnInfo(name = "verse")
    val verse: Int,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "search_text")
    val searchText: String
)

@Fts4(
    contentEntity = VerseEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"],
    prefix = [2, 3, 4]
)
@Entity(tableName = "verses_fts")
data class VerseFtsEntity(
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "search_text")
    val searchText: String
)

/**
 * Projection returned by direct FTS4 queries and Scripture reference lookups.
 */
data class VerseWithBookProjection(
    val id: Int,
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    val bookName: String,
    val bookAbbr: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)
