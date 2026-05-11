package com.example.mypetapp.data

import com.example.mypetapp.screens.Pet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val petsCollection = firestore.collection("pets")
    private val usersCollection = firestore.collection("users")

    suspend fun createUserProfile(userId: String, email: String, name: String) {
        try {
            val userProfile = hashMapOf(
                "fullName" to name,
                "email" to email,
                "ownerId" to userId
            )
            usersCollection.document(userId).set(userProfile).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPets(userId: String): Flow<List<Pet>> = callbackFlow {
        val subscription = petsCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pets = snapshot.toObjects(Pet::class.java)
                    trySend(pets)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addPet(pet: Pet) {
        try {
            val docRef = petsCollection.document()
            val petWithId = pet.copy(id = docRef.id)
            docRef.set(petWithId).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updatePet(pet: Pet) {
        try {
            if (pet.id.isNotEmpty()) {
                petsCollection.document(pet.id).set(pet).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePet(petId: String) {
        try {
            petsCollection.document(petId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}