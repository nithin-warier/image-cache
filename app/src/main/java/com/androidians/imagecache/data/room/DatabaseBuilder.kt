package com.androidians.imagecache.data.room

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {

    private var INSTANCE: ImageCacheDatabase? = null
    private const val DATABASE_NAME = "image_cache"

    fun getInstance(context: Context): ImageCacheDatabase {
        if (INSTANCE == null) {
            synchronized(ImageCacheDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            ImageCacheDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

}