package com.androidians.imagecache.data.room

import androidx.room.RoomDatabase
import com.androidians.imagecache.data.room.dao.ImageUrlDao

abstract class ImageCacheDatabase : RoomDatabase() {
    abstract fun imageUrlDao(): ImageUrlDao
}