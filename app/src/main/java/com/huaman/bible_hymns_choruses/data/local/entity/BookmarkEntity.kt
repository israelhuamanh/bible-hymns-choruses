package com.huaman.bible_hymns_choruses.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "item_type")
    val itemType: String, // "VERSE" | "HYMN" | "CHORUS"
    @ColumnInfo(name = "reference_id")
    val referenceId: Int,
    @ColumnInfo(name = "title_preview")
    val titlePreview: String,
    @ColumnInfo(name = "content_preview")
    val contentPreview: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
