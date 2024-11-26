package com.deo.todo_app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.deo.todo_app.data.local.dao.GalleryDao
import com.deo.todo_app.helper.FirebaseStorageHelper
import com.deo.todo_app.model.Gallery
import com.deo.todo_app.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class GalleryRepository(
    private val galleryDao: GalleryDao, private val firebaseStorage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) {
    suspend fun insertImage(image: Gallery): Long {
        return galleryDao.insertImage(image)
    }

    suspend fun getAllImages(): List<Gallery> {
        return galleryDao.getAllImages()
    }

    fun uploadMediaToFirebase(media: Gallery, onResult: (Boolean, String?) -> Unit) {
        val file = File(media.localPath)
        val storageRef = firebaseStorage.reference.child("media/${file.name}")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        storageRef.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val mediaData = hashMapOf(
                        "firebaseUrl" to uri.toString(),
                        "localPath" to media.localPath,
                        "type" to media.type
                    )

                    firestore.collection("users").document(userId).collection("gallery")
                        .document(media.id.toString())
                        .set(mediaData)
                        .addOnSuccessListener {
                            onResult(true, uri.toString())
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Failed to save in Firestore: ${e.message}")
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, "Failed to upload to Firebase Storage: ${it.message}")
            }
    }

    //get all media from firestore after login
    fun syncMediaFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("gallery")
            .get()
            .addOnSuccessListener { documents ->
                val gallerys = documents.mapNotNull { document ->
                    document.toObject(Gallery::class.java)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    galleryDao.insertAll(gallerys)

                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchTasksAndSaveToRoom", "Error fetching tasks: ${exception.message}")
            }
    }

    //sync offline data to firestore
    suspend fun syncOfflineGallery() {
        val unsyncedGallery = galleryDao.getUnsyncedGallery()
        for (item in unsyncedGallery) {
            val forResult = uploadMediaToFirebaseSuspend(item)
            if (forResult != null) {
                galleryDao.updateGallery(item.copy(synced = true, firebaseUrl = forResult))
            }
        }
    }

    private suspend fun uploadMediaToFirebaseSuspend(item: Gallery): String? {
        return suspendCancellableCoroutine { continuation ->
            uploadMediaToFirebase(item, onResult = { success, result ->
                if (continuation.isActive) {
                    if (success) {
                        continuation.resume(result) // Kembalikan result jika success true
                    } else {
                        continuation.resume(null) // Kembalikan null jika success false
                    }
                }
            })
        }
    }
}