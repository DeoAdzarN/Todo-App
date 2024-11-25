package com.deo.todo_app.helper

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FirebaseStorageHelper {
    private val storage = FirebaseStorage.getInstance()

    fun uploadFile(filePath: String, onComplete: (String?, String?) -> Unit) {
        val file = File(filePath)
        val storageRef = storage.reference.child("attachments/${System.currentTimeMillis()}")
        storageRef.putFile(Uri.fromFile(file)).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(file.absolutePath, task.result.toString())
            } else {
                onComplete(null, null)
            }
        }
    }
}