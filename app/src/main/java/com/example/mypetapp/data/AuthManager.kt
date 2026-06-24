package com.example.mypetapp.data

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mfaSignInVerificationId: String = ""
    private var multiFactorResolver: MultiFactorResolver? = null
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    sealed class LoginResult {
        data class Success(val user: FirebaseUser) : LoginResult()
        data class Requires2FA(val hint: String) : LoginResult()
        data class Failure(val exception: Exception) : LoginResult()
    }

    suspend fun login(
        email: String,
        pass: String,
        activity: android.app.Activity,
        onAutoVerifySuccess: () -> Unit
    ): LoginResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            LoginResult.Success(result.user!!)
        } catch (e: FirebaseAuthMultiFactorException) {
            multiFactorResolver = e.resolver

            val phoneHint = multiFactorResolver?.hints?.firstOrNull() as? PhoneMultiFactorInfo
            val hintText = phoneHint?.phoneNumber ?: "your registered phone"

            sendMfaSms(activity, onAutoVerifySuccess)

            LoginResult.Requires2FA(hintText)
        } catch (e: Exception) {
            LoginResult.Failure(e)
        }
    }

    private fun sendMfaSms(
        activity: android.app.Activity,
        onAutoVerifySuccess: () -> Unit
    ) {
        val resolver = multiFactorResolver ?: return
        val hints = resolver.hints
        val phoneHint = hints.firstOrNull() as? PhoneMultiFactorInfo ?: return

        val options = PhoneAuthOptions.newBuilder(auth)
            .setMultiFactorHint(phoneHint)
            .setMultiFactorSession(resolver.session)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val currentResolver = multiFactorResolver ?: return@launch
                            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
                            currentResolver.resolveSignIn(assertion).await()

                            multiFactorResolver = null
                            mfaSignInVerificationId = ""

                            onAutoVerifySuccess()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    e.printStackTrace()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    mfaSignInVerificationId = verificationId
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    suspend fun verify2FACode(smsCode: String): Result<FirebaseUser> {
        return try {
            val resolver = multiFactorResolver ?: return Result.failure(Exception("No 2FA session found"))

            val credential = PhoneAuthProvider.getCredential(mfaSignInVerificationId, smsCode)
            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)

            val result = resolver.resolveSignIn(assertion).await()

            multiFactorResolver = null
            mfaSignInVerificationId = ""

            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun start2FAEnrollment(
        phoneNumber: String,
        activity: android.app.Activity,
        onCodeSent: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("No user logged in for 2FA"))
            return
        }

        user.multiFactor.session.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onFailure(task.exception ?: Exception("Could not generate MFA."))
                return@addOnCompleteListener
            }

            val mfaSession = task.result

            try {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                        override fun onVerificationFailed(e: FirebaseException) {
                            onFailure(e)
                        }
                        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            onCodeSent(verificationId)
                        }
                    })
                    .setMultiFactorSession(mfaSession)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    suspend fun finalize2FAEnrollment(verificationId: String, smsCode: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)

            user.multiFactor.enroll(assertion, "pet owner").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user

            if (user != null) {
                user.sendEmailVerification().await()
                Result.success(user)
            } else {
                Result.failure(Exception("User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}