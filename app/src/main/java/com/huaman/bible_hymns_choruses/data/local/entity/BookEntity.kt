package com.huaman.bible_hymns_choruses.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "testament")
    val testament: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "abbreviation")
    val abbreviation: String,
    @ColumnInfo(name = "search_name")
    val searchName: String,
    @ColumnInfo(name = "chapter_count")
    val chapterCount: Int
)
