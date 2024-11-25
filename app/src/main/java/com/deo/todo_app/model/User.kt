package com.deo.todo_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val picturePath: String?,
    val pictureUrl: String?,
    val email: String,
    val isLoggedIn: Boolean,
    val isSynced:Boolean
)
