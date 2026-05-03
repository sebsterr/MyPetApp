package com.example.mypetapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypetapp.data.AuthManager
import com.example.mypetapp.data.FirestoreManager
import com.example.mypetapp.data.StorageManager
import com.example.mypetapp.screens.Pet
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PetViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val firestoreManager = FirestoreManager()
    private val storageManager = StorageManager()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authManager.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observePets()
    }

    private fun observePets() {
        viewModelScope.launch {
            currentUser.collectLatest { user ->
                if (user != null) {
                    firestoreManager.getPets(user.uid).collect { petList ->
                        _pets.value = petList
                    }
                } else {
                    _pets.value = emptyList()
                }
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.login(email, pass)
            _isLoading.value = false
            result.onSuccess {
                _currentUser.value = it
                onSuccess()
            }.onFailure {
                onError(it.message ?: "A apărut o eroare la autentificare")
            }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.register(email, pass)
            _isLoading.value = false
            result.onSuccess {
                _currentUser.value = it
                onSuccess()
            }.onFailure {
                onError(it.message ?: "A apărut o eroare la înregistrare")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        authManager.logout()
        _currentUser.value = null
        onSuccess()
    }

    fun addPet(name: String, type: String, breed: String, birthDate: String, weight: Double, imageUri: Uri?) {
        val userId = currentUser.value?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            var uploadedImageUrl = ""
            if (imageUri != null) {
                uploadedImageUrl = storageManager.uploadPetImage(imageUri)
            }
            
            val newPet = Pet(
                name = name,
                type = type,
                breed = breed,
                birthDate = birthDate,
                weight = weight,
                ownerId = userId,
                imageUrl = uploadedImageUrl
            )
            firestoreManager.addPet(newPet)
            _isLoading.value = false
        }
    }

    fun updatePet(pet: Pet) {
        viewModelScope.launch {
            firestoreManager.updatePet(pet)
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            firestoreManager.deletePet(petId)
        }
    }
}
