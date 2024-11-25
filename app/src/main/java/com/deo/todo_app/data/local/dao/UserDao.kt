package com.deo.todo_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deo.todo_app.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser():User

    @Query("UPDATE users SET name = :name, isLoggedIn = :isLoggedIn WHERE userId = :id")
     fun updateUser(
        id: String,
        name: String,
        isLoggedIn: Boolean
    )

     @Update
     suspend fun updateUser(user: User)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}