package com.deo.todo_app.helper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTimeHelper {
    fun convertToMillis(dateString: String): Long? {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertToDateString(millis: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val date = Date(millis)
        return dateFormat.format(date)
    }

    fun convertToDateOnly(millis: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date(millis)
        return dateFormat.format(date)
    }

    fun convertToTimeOnly(millis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(millis)
        return dateFormat.format(date)
    }
}