package com.example.mypetapp.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypetapp.viewmodel.FeedingViewModel
import java.util.*

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
    var editingTask by remember { mutableStateOf<FeedingTask?>(null) }

    LaunchedEffect(petId) {
        viewModel.getFeedingSchedule(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feeding Schedule: $petName", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add meal", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (schedule.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No meals scheduled. Press +", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(schedule) { task ->
                    FeedingTaskCard(
                        task = task,
                        onEdit = {
                            editingTask = task
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteFeedingTask(context, petId, task.id)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddFeedingDialog(
            existingTask = editingTask,
            onDismiss = {
                showDialog = false
                editingTask = null
            },
            onSave = { time, food, qty, id ->
                viewModel.saveFeedingTask(context, petId, petName, time, food, qty, id)
                showDialog = false
                editingTask = null
            }
        )
    }
}

@Composable
fun FeedingTaskCard(
    task: FeedingTask,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.time,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${task.foodType} • ${task.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddFeedingDialog(
    existingTask: FeedingTask? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    var foodType by remember { mutableStateOf(existingTask?.foodType ?: "") }
    var quantity by remember { mutableStateOf(existingTask?.quantity ?: "") }
    var selectedTime by remember { mutableStateOf(existingTask?.time ?: "08:00") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTask == null) "Schedule a meal" else "Edit meal") },
        text = {
            Column {
                OutlinedTextField(
                    value = foodType,
                    onValueChange = { foodType = it },
                    label = { Text("Food type") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(context, { _, hour, minute ->
                        selectedTime = String.format("%02d:%02d", hour, minute)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                }) {
                    Text("Time: $selectedTime")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedTime, foodType, quantity, existingTask?.id) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
