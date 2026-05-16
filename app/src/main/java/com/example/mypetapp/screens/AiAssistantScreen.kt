package com.example.mypetapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mypetapp.viewmodel.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    petName: String,
    petContext: String,
    viewModel: AiViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var userQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val todayDate = formatter.format(java.util.Date())
    LaunchedEffect(petContext) {
            val promptTemplate = """
            You are PetAI, an expert virtual veterinary assistant. 
            Provide helpful, accurate advice regarding pet care, nutrition, and health.
            Always add a friendly tone and remind the user to consult a physical vet if the issue seems critical.
            Today's date is $todayDate.
            Current pet context:
            $petContext

            IF THE PROMPT BELOW DOES NOT HAVE ANY KIND OF PET RELATED INFORMATION, ANSWER WITH: "Please provide questions about your pet."
            Answer in the language you are asked after this.
        """.trimIndent()

        viewModel.initializeChatWithPet(promptTemplate)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$petName's AI Pet Assistant") }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                messages.forEach { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(if (message.isUser) Alignment.End else Alignment.Start)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (message.isUser) 16.dp else 0.dp,
                                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                                )
                            )
                            .background(
                                if (message.isUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }


                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start)
                            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQuery,
                    onValueChange = { userQuery = it },
                    label = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.askGemini(userQuery)
                        userQuery = ""
                    },
                    enabled = !isLoading && userQuery.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}