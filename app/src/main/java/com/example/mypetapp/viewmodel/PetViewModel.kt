package com.example.mypetapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypetapp.data.AuthManager
import com.example.mypetapp.data.FirestoreManager
import com.example.mypetapp.data.StorageManager
import com.example.mypetapp.screens.Pet
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PetViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val firestoreManager = FirestoreManager()
    private val storageManager = StorageManager()

    private val _currentUser = MutableStateFlow(authManager.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val pets: StateFlow<List<Pet>> = currentUser
        .flatMapLatest { user ->
            if (user != null) firestoreManager.getPets(user.uid)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun register(email: String, pass: String, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.register(email, pass)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    firestoreManager.createUserProfile(user.uid, email, name)
                    _currentUser.value = user
                    onResult(true)
                } else {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.login(email, pass)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                onResult(true)
            } else {
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun addPet(name: String, type: String, breed: String, date: String, weight: Double, gender: String, isNeutered: String, uri: Uri?) {
        viewModelScope.launch {
            val userId = currentUser.value?.uid ?: return@launch

            _isLoading.value = true
            val imageUrl = if (uri != null) storageManager.uploadPetImage(uri) else ""

            val newPet = Pet(
                id = "",
                name = name,
                type = type,
                breed = breed,
                birthDate = date,
                weight = weight,
                gender = gender,
                isNeutered = isNeutered,
                ownerId = userId,
                imageUrl = imageUrl
            )
            firestoreManager.addPet(newPet)
            _isLoading.value = false
        }
    }

    fun updatePet(petId: String, name: String, type: String, breed: String, date: String, weight: Double, gender: String, isNeutered: String, newUri: Uri?, oldImageUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true

            var finalImageUrl = oldImageUrl

            if (newUri != null) {
                if (oldImageUrl.isNotEmpty()) {
                    storageManager.deleteImage(oldImageUrl)
                }
                finalImageUrl = storageManager.uploadPetImage(newUri)
            }

            val updatedPet = Pet(
                id = petId,
                name = name,
                type = type,
                breed = breed,
                birthDate = date,
                weight = weight,
                gender = gender,
                isNeutered = isNeutered,
                ownerId = currentUser.value?.uid ?: "",
                imageUrl = finalImageUrl
            )

            firestoreManager.updatePet(updatedPet)
            _isLoading.value = false
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            firestoreManager.deletePet(petId)
        }
    }

    fun logout(onComplete: () -> Unit) {
        authManager.logout()
        _currentUser.value = null
        onComplete()
    }
}