package com.deo.todo_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deo.todo_app.model.Task

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE synced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): LiveData<Task>

    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getAllTaskByStatus(status: String): LiveData<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE status = :status
        ORDER BY date ASC
        LIMIT :limit
    """)
    fun getLimitedTaskByStatus(status: String, limit: Int): LiveData<List<Task>>

    @Query("UPDATE tasks SET title = :title, description = :description, date = :date, reminder = :reminder, status = :status,  synced = :isSynced WHERE id = :id")
    suspend fun updateTaskFields(
        id: String,
        title: String,
        description: String,
        date: Long,
        reminder: Long,
        status: String,
        isSynced: Boolean
    )

    @Update
    suspend fun updateTask(task: Task)

    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE date BETWEEN :startOfMonth AND :endOfMonth AND status = :status
    """)
    fun getCountByDateAndStatus(
        startOfMonth: Long,
        endOfMonth: Long,
        status: String
    ): LiveData<Int>

    @Query("SELECT * FROM tasks WHERE date >= :startMillis AND date < :endMillis")
    suspend fun getTasksInRange(startMillis: Long, endMillis: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE date = :dateMillis")
    fun getTasksByDate(dateMillis: Long): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE status IN ('Pending', 'On Progress')")
    suspend fun getOnGoingTasks(): List<Task>

    @Insert
    suspend fun insertAll(tasks: List<Task>)
}