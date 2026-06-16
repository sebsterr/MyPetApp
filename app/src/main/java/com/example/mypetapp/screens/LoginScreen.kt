package com.example.mypetapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypetapp.viewmodel.PetViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: PetViewModel
) {
    val context = LocalContext.current
    val activity = remember(context) { context as android.app.Activity }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isSmsCodeRequired by remember { mutableStateOf(false) }
    var smsCode by remember { mutableStateOf("") }
    var phoneHint by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSmsCodeRequired) "Two-Factor Verification" else if (isRegisterMode) "Create Account" else "Login",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isSmsCodeRequired) {

            Text(
                text = "A verification code has been sent to $phoneHint. Please enter it below:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = smsCode,
                onValueChange = { if (it.length <= 6) smsCode = it },
                label = { Text("SMS Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        errorMessage = null

                        viewModel.verify2FA(smsCode) { success ->
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Invalid verification code. Try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = smsCode.length == 6
                ) {
                    Text("Verify Code")
                }

                TextButton(
                    onClick = {
                        isSmsCodeRequired = false
                        smsCode = ""
                        errorMessage = null
                    }
                ) {
                    Text("Back to Login")
                }
            }

        } else {

            if (isRegisterMode) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        errorMessage = null

                        if (isRegisterMode) {
                            viewModel.register(email, password, fullName) { success ->
                                if (success) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Registration error. Try again."
                                }
                            }
                        } else {
                            viewModel.login(email, password, activity) { result ->
                                when (result) {
                                    "SUCCESS" -> onLoginSuccess()
                                    "REQUIRES_2FA" -> {
                                        phoneHint = viewModel.getPhoneHint()
                                        isSmsCodeRequired = true
                                    }
                                    else -> {
                                        errorMessage = "Incorrect email or password."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotEmpty() && password.isNotEmpty()
                ) {
                    Text(if (isRegisterMode) "Sign Up" else "Login")
                }

                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                    }
                ) {
                    Text(if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Create one")
                }
            }
        }
    }
}