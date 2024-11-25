package com.deo.todo_app.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.deo.todo_app.data.local.dao.AttachmentDao
import com.deo.todo_app.data.local.dao.TaskDao
import com.deo.todo_app.helper.FirebaseStorageHelper
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Gallery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttachmentRepository(private val attachmentDao: AttachmentDao, private val firestore: FirebaseFirestore) {

    suspend fun insertAttachment(attachment: List<Attachment>, onResult: (Boolean, String?) -> Unit) {
        attachmentDao.insertAttachment(attachment)
        syncAttachmentWithFirestore(attachment,onResult)
    }

    suspend fun getAllAttachment(): List<Attachment> {
        return withContext(Dispatchers.IO) {
            attachmentDao.getAllAttachment()
        }
    }

    suspend fun removeAttachmentByTaskId(taskId: List<String>) {
        taskId.forEach {
            attachmentDao.removeAttachmentByTaskId(it)
        }
    }

    suspend fun getAttachmentsByTaskId(taskId: String): List<Attachment> {
        return withContext(Dispatchers.IO) {
            attachmentDao.getAttachmentsByTaskId(taskId)
        }
    }

    private fun syncAttachmentWithFirestore(attachment: List<Attachment>, onResult: (Boolean, String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var successfulWrites = 0
        val totalWrites = attachment.size
        attachment.forEach {
            FirebaseStorageHelper().uploadFile(it.path){ path, uri ->
                if (path != null && uri != null) {
                    successfulWrites++
                    CoroutineScope(Dispatchers.IO).launch {
                        it.url = uri.toString()
                        firestore.collection("users").document(userId)
                            .collection("attachments").document(it.id).set(it)
                        if (successfulWrites == totalWrites) {
                            onResult(true,null)
                        }
                    }

                }else{
                    onResult(false,"Failed to upload file")
                }
            }

        }

    }

    fun syncMediaFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("attachments")
            .get()
            .addOnSuccessListener { documents ->
                val attachment = documents.mapNotNull { document ->
                    document.toObject(Attachment::class.java)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    attachmentDao.insertAttachment(attachment)

                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchTasksAndSaveToRoom", "Error fetching tasks: ${exception.message}")
            }
    }

}