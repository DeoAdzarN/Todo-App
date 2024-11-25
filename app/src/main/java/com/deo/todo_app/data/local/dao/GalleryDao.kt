package com.deo.todo_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deo.todo_app.model.Gallery
import com.deo.todo_app.model.Task

@Dao
interface GalleryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(gallery: Gallery): Long

    @Query("SELECT * FROM gallery")
    suspend fun getAllImages(): List<Gallery>
    @Update
    suspend fun updateGallery(gallery: Gallery)

    @Insert
    suspend fun insertAll(gallery: List<Gallery>)

    @Query("SELECT * FROM gallery WHERE synced = 0")
    suspend fun getUnsyncedGallery(): List<Gallery>
}