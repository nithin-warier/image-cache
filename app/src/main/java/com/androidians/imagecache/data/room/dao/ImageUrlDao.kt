package com.androidians.imagecache.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidians.imagecache.data.room.entity.ImageUrlEntity

@Dao
interface ImageUrlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(imageUrlEntity: ImageUrlEntity)

    @Query("SELECT * FROM image_url")
    fun getAll(): List<ImageUrlEntity>

}