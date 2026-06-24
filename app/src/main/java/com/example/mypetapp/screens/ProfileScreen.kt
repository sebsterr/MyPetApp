package com.example.mypetapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypetapp.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: PetViewModel
) {
    val context = LocalContext.current
    val activity = remember(context) { context as android.app.Activity }

    val userState by viewModel.currentUser.collectAsState()

    val fullNameFromDb by viewModel.fullName.collectAsState()

    LaunchedEffect(userState?.uid) {
        if (userState != null) {
            viewModel.fetchUserFullName()
            viewModel.checkEmailVerificationStatus { }
        }
    }

    val email = userState?.email ?: "No email available"

    val displayName = fullNameFromDb

    val isLoading by viewModel.isLoading.collectAsState()

    val isEmailVerified = userState?.isEmailVerified == true
    var emailWarningMessage by remember { mutableStateOf<String?>(null) }

    val isMfaEnabled by remember(userState) {
        derivedStateOf {
            userState?.multiFactor?.enrolledFactors?.isNotEmpty() == true
        }
    }
    var showMfaDialog by remember { mutableStateOf(false) }
    var enrollmentStep by remember { mutableStateOf(1) }

    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var dialogErrorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account Settings") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = ShapeDefaults.Large,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = {},
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Two-Factor Auth (MFA)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = if (isMfaEnabled) "Secured via SMS" else "Disabled (Unsecure)",
                            fontSize = 14.sp,
                            color = if (isMfaEnabled) Color(0xFF4CAF50) else Color.Gray
                        )
                    }

                    Switch(
                        checked = isMfaEnabled,
                        onCheckedChange = { isChecking ->
                            if (isChecking) {
                                if (isEmailVerified) {
                                    emailWarningMessage = null
                                    enrollmentStep = 1
                                    phoneNumber = ""
                                    smsCode = ""
                                    dialogErrorMessage = null
                                    showMfaDialog = true
                                } else {
                                    emailWarningMessage = "You must verify your email address before enabling MFA. Please check your inbox!"
                                }
                            } else {
                                viewModel.disableMfa { success ->
                                    if (!success) {
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (emailWarningMessage != null && !isEmailVerified) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = emailWarningMessage!!, color = Color(0xFF856404), fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.sendVerificationEmail { success ->
                                    if (success) {
                                        emailWarningMessage = "Verification email sent! Please check your inbox and click the link."
                                    } else {
                                        emailWarningMessage = "Failed to send email. You might be making too many requests. Try again later."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send Verification Email")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.checkEmailVerificationStatus { verified ->
                                    if (verified) {
                                        emailWarningMessage = null
                                    } else {
                                        emailWarningMessage = "Still not verified. Please click the verification link in your email inbox and try again."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I have verified my email")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogoutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }

    if (showMfaDialog) {
        AlertDialog(
            onDismissRequest = { showMfaDialog = false },
            title = { Text(if (enrollmentStep == 1) "Enable 2FA Security" else "Verify SMS Code") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (enrollmentStep == 1) {
                        Text("Enter your phone number (including country code, e.g., +407xxxxxxxx):", modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Enter the 6-digit verification code sent to your phone:", modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = smsCode,
                            onValueChange = { if (it.length <= 6) smsCode = it },
                            label = { Text("Verification Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    dialogErrorMessage?.let {
                        Text(text = it, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogErrorMessage = null
                        if (enrollmentStep == 1) {
                            if (phoneNumber.startsWith("+")) {
                                viewModel.startMfaEnrollment(
                                    phoneNumber = phoneNumber,
                                    activity = activity
                                ) { success ->
                                    if (success) {
                                        enrollmentStep = 2
                                    } else {
                                        dialogErrorMessage = "Failed to send SMS. Check format or configuration."
                                    }
                                }
                            } else {
                                dialogErrorMessage = "Number must start with country code (ex: +40...)"
                            }
                        } else {
                            viewModel.finalizeMfaEnrollment(smsCode) { success ->
                                if (success) {
                                    showMfaDialog = false
                                } else {
                                    dialogErrorMessage = "Invalid code. Please try again."
                                }
                            }
                        }
                    },
                    enabled = if (enrollmentStep == 1) phoneNumber.isNotEmpty() else smsCode.length == 6
                ) {
                    Text(if (enrollmentStep == 1) "Send Code" else "Verify & Activate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMfaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}