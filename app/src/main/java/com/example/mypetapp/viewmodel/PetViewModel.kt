package com.example.mypetapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypetapp.data.AuthManager
import com.example.mypetapp.data.FirestoreManager
import com.example.mypetapp.data.StorageManager
import com.example.mypetapp.screens.Pet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class PetViewModel : ViewModel() {
    private val authManager = AuthManager()
    private val firestoreManager = FirestoreManager()
    private val storageManager = StorageManager()

    private var currentPhoneHint: String = ""
    fun getPhoneHint(): String = currentPhoneHint

    private var mfaEnrollmentVerificationId: String = ""

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

    fun login(email: String, pass: String, activity: android.app.Activity, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = authManager.login(email, pass, activity)) {
                is AuthManager.LoginResult.Success -> {
                    _currentUser.value = result.user
                    onResult("SUCCESS")
                }
                is AuthManager.LoginResult.Requires2FA -> {
                    currentPhoneHint = result.hint
                    onResult("REQUIRES_2FA")
                }
                is AuthManager.LoginResult.Failure -> {
                    onResult("FAILURE")
                }
            }
            _isLoading.value = false
        }
    }

    fun verify2FA(smsCode: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = authManager.verify2FACode(smsCode)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                onResult(true)
            } else {
                onResult(false)
            }

            _isLoading.value = false
        }
    }

    fun isMfaEnabled(): Boolean {
        val user = authManager.currentUser ?: return false
        return user.multiFactor.enrolledFactors.isNotEmpty()
    }

    fun startMfaEnrollment(phoneNumber: String, activity: android.app.Activity, onSmsSent: (Boolean) -> Unit) {
        _isLoading.value = true

        val user = authManager.currentUser

        if (user != null) {
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    authManager.start2FAEnrollment(
                        phoneNumber = phoneNumber,
                        activity = activity,
                        onCodeSent = { verificationId ->
                            mfaEnrollmentVerificationId = verificationId
                            _isLoading.value = false
                            onSmsSent(true)
                        },
                        onFailure = { exception ->
                            exception.printStackTrace()
                            _isLoading.value = false
                            onSmsSent(false)
                        }
                    )
                } else {
                    reloadTask.exception?.printStackTrace()
                    _isLoading.value = false
                    onSmsSent(false)
                }
            }
        } else {
            _isLoading.value = false
            onSmsSent(false)
        }
    }

    fun finalizeMfaEnrollment(smsCode: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.finalize2FAEnrollment(mfaEnrollmentVerificationId, smsCode)
            if (result.isSuccess) {
                _currentUser.value = authManager.currentUser
                onResult(true)
            } else {
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun disableMfa(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authManager.currentUser
                if (user != null && user.multiFactor.enrolledFactors.isNotEmpty()) {
                    val factorInfo = user.multiFactor.enrolledFactors.first()
                    user.multiFactor.unenroll(factorInfo).await()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    fun checkEmailVerificationStatus(onResult: (Boolean) -> Unit) {
        val user = authManager.currentUser
        if (user != null) {
            _isLoading.value = true
            user.reload().addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _currentUser.value = authManager.currentUser
                    onResult(authManager.currentUser?.isEmailVerified == true)
                } else {
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }
    fun sendVerificationEmail(onResult: (Boolean) -> Unit) {
        val user = authManager.currentUser
        if (user != null) {
            _isLoading.value = true
            user.sendEmailVerification().addOnCompleteListener { task ->
                _isLoading.value = false
                onResult(task.isSuccessful)
            }
        } else {
            onResult(false)
        }
    }
    fun deletePet(petId: String) {
        viewModelScope.launch {
            firestoreManager.deletePet(petId)
        }
    }

    fun logout() {
        authManager.logout()
        _currentUser.value = null
    }

    fun logout(onComplete: () -> Unit) {
        authManager.logout()
        _currentUser.value = null
        onComplete()
    }
}