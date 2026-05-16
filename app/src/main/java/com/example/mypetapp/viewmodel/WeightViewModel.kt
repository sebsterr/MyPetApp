package com.example.mypetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mypetapp.screens.WeightEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeightViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _weightHistory = MutableStateFlow<List<WeightEntry>>(emptyList())
    val weightHistory: StateFlow<List<WeightEntry>> = _weightHistory

    fun getWeightHistory(petId: String) {
        db.collection("pets").document(petId).collection("weight_history")
            .orderBy("date")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _weightHistory.value = snapshot.toObjects(WeightEntry::class.java)
                }
            }
    }

    fun addWeightEntry(petId: String, weight: Float) {
        val ref = db.collection("pets").document(petId).collection("weight_history").document()
        val entry = WeightEntry(id = ref.id, weight = weight)
        ref.set(entry)

        db.collection("pets").document(petId).update("weight", weight)
    }
}