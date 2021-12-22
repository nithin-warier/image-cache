package com.androidians.imagecache.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_url")
data class ImageUrlEntity (
    @PrimaryKey @ColumnInfo(name = "url") var imageUrl: String
)