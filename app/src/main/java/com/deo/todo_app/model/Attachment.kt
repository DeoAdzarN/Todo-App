package com.deo.todo_app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "attachment")
data class Attachment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var taskId: String = "",
    val type: String = "image", // "image", "video"
    var url: String = "",
    val path: String = "",
    val synced: Boolean = false
)
