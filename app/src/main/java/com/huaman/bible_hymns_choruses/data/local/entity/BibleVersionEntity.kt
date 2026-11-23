package com.huaman.bible_hymns_choruses.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_versions")
data class BibleVersionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "full_title")
    val fullTitle: String,
    @ColumnInfo(name = "copyright")
    val copyright: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
)
