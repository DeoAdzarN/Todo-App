package com.deo.todo_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title:String,
    val description: String,
    val date: Long,
    val reminder: Long,
    val status: String,
    val synced: Boolean = false
) : Serializable {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        title = "",
        description = "",
        date = 0L,
        reminder = 0L,
        status = "Pending",
        synced = false
    )
}
