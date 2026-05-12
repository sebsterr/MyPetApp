package com.example.mypetapp.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mypetapp.screens.FeedingTask
import com.example.mypetapp.viewmodel.FeedingViewModel
import java.util.*
import androidx.compose.ui.Alignment
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun FeedingScheduleScreen(
petId: String,
petName: String,
viewModel: FeedingViewModel
) {
    val context = LocalContext.current
    val schedule by viewModel.schedule.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.getFeedingSchedule(petId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Program mese: $petName") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adaugă masă")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(schedule) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Ora: ${task.time}", style = MaterialTheme.typography.titleLarge)
                            Text(text = "${task.foodType} - ${task.quantity}")
                        }

                        IconButton(onClick = {
                            viewModel.deleteFeedingTask(petId, task.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Șterge masa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddFeedingDialog(
            onDismiss = { showDialog = false },
            onSave = { time, food, qty ->
                viewModel.saveFeedingTask(context, petId, petName, time, food, qty)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddFeedingDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var foodType by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("08:00") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programează o masă") },
        text = {
            Column {
                OutlinedTextField(value = foodType, onValueChange = { foodType = it }, label = { Text("Tip mâncare") })
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantitate (ex: 50g)") })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(context, { _, hour, minute ->
                        selectedTime = String.format("%02d:%02d", hour, minute)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                }) {
                    Text("Alege Ora: $selectedTime")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedTime, foodType, quantity) }) { Text("Salvează") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )
}