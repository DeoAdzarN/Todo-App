package com.deo.todo_app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deo.todo_app.data.local.dao.AttachmentDao
import com.deo.todo_app.data.local.dao.GalleryDao
import com.deo.todo_app.data.local.dao.TaskDao
import com.deo.todo_app.data.local.dao.UserDao
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Gallery
import com.deo.todo_app.model.Task
import com.deo.todo_app.model.User
import com.deo.todo_app.utils.Converter

@Database(entities = [Task::class, User::class, Attachment::class, Gallery::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun galleryDao(): GalleryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todoDatabase"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun deleteDatabase(context: Context) {
            context.deleteDatabase("todoDatabase")
            INSTANCE = null
        }
    }
}