package com.deo.todo_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deo.todo_app.model.Attachment

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: List<Attachment>)

    @Query("SELECT * FROM attachment WHERE taskId = :taskId")
    fun getAttachmentsByTaskId(taskId: String): List<Attachment>

    @Query("DELETE FROM attachment WHERE taskId = :taskId")
    suspend fun removeAttachmentByTaskId(taskId: String)

    @Query("SELECT * FROM attachment")
    fun getAllAttachment(): List<Attachment>
}