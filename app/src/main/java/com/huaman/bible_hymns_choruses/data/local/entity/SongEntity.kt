package com.huaman.bible_hymns_choruses.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["type", "number"], name = "idx_songs_type_number")
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "type")
    val type: String, // "HYMN" | "CHORUS"
    @ColumnInfo(name = "number")
    val number: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "subtitle_en")
    val subtitleEn: String?,
    @ColumnInfo(name = "tonality")
    val tonality: String?,
    @ColumnInfo(name = "category")
    val category: String?,
    @ColumnInfo(name = "lyrics")
    val lyrics: String,
    @ColumnInfo(name = "search_text")
    val searchText: String
)

@Fts4(
    contentEntity = SongEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"],
    prefix = [2, 3, 4]
)
@Entity(tableName = "songs_fts")
data class SongFtsEntity(
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "lyrics")
    val lyrics: String,
    @ColumnInfo(name = "search_text")
    val searchText: String
)

/**
 * Lightweight projection without loading the full `lyrics` block for 120 fps LazyColumn scrolling.
 */
data class SongSummaryProjection(
    val id: Int,
    val type: String,
    val number: Int,
    val title: String,
    @ColumnInfo(name = "subtitle_en")
    val subtitleEn: String?,
    val tonality: String?,
    val category: String?
)
