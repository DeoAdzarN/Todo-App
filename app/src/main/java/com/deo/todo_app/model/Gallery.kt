package com.deo.todo_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "gallery")
data class Gallery(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val localPath: String = "",
    val firebaseUrl: String? = null,
    val type: String = "image"
)