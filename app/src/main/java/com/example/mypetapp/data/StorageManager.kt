package com.example.mypetapp.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadPetImage(uri: Uri): String {
        return try {
            val fileName = "pet_images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    suspend fun deleteImage(imageUrl: String) {
        if (imageUrl.isEmpty()) return
        try {

            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}
