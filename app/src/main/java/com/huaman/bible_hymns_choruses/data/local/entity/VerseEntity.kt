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
        ),
        ForeignKey(
            entity = BibleVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["version_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["version_id", "book_id", "chapter", "item_order"], name = "idx_verses_location"),
        Index(value = ["version_id", "book_id", "chapter", "verse", "verse_end"], name = "idx_verses_ref"),
        Index(value = ["book_id"], name = "idx_verses_book_id")
    ]
)
data class VerseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "version_id")
    val versionId: String,
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    @ColumnInfo(name = "chapter")
    val chapter: Int,
    @ColumnInfo(name = "verse")
    val verse: Int,
    @ColumnInfo(name = "verse_end")
    val verseEnd: Int,
    @ColumnInfo(name = "verse_label")
    val verseLabel: String,
    @ColumnInfo(name = "item_order")
    val itemOrder: Int,
    @ColumnInfo(name = "heading")
    val heading: String?,
    @ColumnInfo(name = "label")
    val label: String?,
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
    @ColumnInfo(name = "search_text")
    val searchText: String
)

/**
 * Projection returned by direct FTS4 queries and Scripture reference lookups.
 */
data class VerseWithBookProjection(
    val id: Int,
    @ColumnInfo(name = "version_id")
    val versionId: String,
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    val bookName: String,
    val bookAbbr: String,
    val chapter: Int,
    val verse: Int,
    @ColumnInfo(name = "verse_end")
    val verseEnd: Int,
    @ColumnInfo(name = "verse_label")
    val verseLabel: String,
    val text: String
)
