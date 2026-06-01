package com.example.mypetapp.screens

import androidx.compose.material.icons.filled.Delete
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mypetapp.viewmodel.FeedingViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScheduleScreen(petId: String, petName: String, viewModel: FeedingViewModel) {
    val context = LocalContext.current

    val schedules by viewModel.schedule.collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Feeding") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("None") }

    var expandedRecurrence by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.getFeedingSchedule(petId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Schedule & Reminders for $petName", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        val types = listOf("Feeding" to "Feeding", "Visit" to "Vet Visit", "Vaccine" to "Vaccine")
        TabRow(selectedTabIndex = types.indexOfFirst { it.first == selectedType }) {
            types.forEach { (typeKey, typeLabel) ->
                Tab(
                    selected = selectedType == typeKey,
                    onClick = {
                        selectedType = typeKey
                        title = ""
                        selectedDate = if (typeKey == "Feeding") "Everyday" else ""
                    },
                    text = { Text(typeLabel) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text(
                    when(selectedType) {
                        "Visit" -> "Reason for Visit (e.g., Checkup)"
                        "Vaccine" -> "Vaccine Name (e.g., Rabies)"
                        else -> "Meal Name (e.g., Kibble)"
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedType != "Feeding") {
            Box(modifier = Modifier.fillMaxWidth().clickable {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    selectedDate = "$y-${m + 1}-$d"
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Event Date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().clickable {
            val cal = Calendar.getInstance()
            TimePickerDialog(context, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (selectedType == "Feeding") "Set Feeding Time" else "Set Appointment Time (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        if (selectedType == "Vaccine") {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expandedRecurrence, onExpandedChange = { expandedRecurrence = !expandedRecurrence }) {
                OutlinedTextField(
                    value = when(recurrence) {
                        "6 Months" -> "Every 6 Months"
                        "1 Year" -> "Every Year"
                        else -> "One-Time Shot"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vaccine Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRecurrence) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedRecurrence, onDismissRequest = { expandedRecurrence = false }) {
                    DropdownMenuItem(text = { Text("One-Time Shot") }, onClick = { recurrence = "None"; expandedRecurrence = false })
                    DropdownMenuItem(text = { Text("Every 6 Months") }, onClick = { recurrence = "6 Months"; expandedRecurrence = false })
                    DropdownMenuItem(text = { Text("Every Year") }, onClick = { recurrence = "1 Year"; expandedRecurrence = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    viewModel.saveFeedingTask(
                        context = context,
                        petId = petId,
                        petName = petName,
                        time = selectedTime.ifBlank { "09:00" },
                        food = title,
                        quantity = if (selectedType == "Feeding") "1 portion" else "",
                        type = selectedType,
                        date = if (selectedType == "Feeding") "Everyday" else selectedDate,
                        recurrence = if (selectedType == "Vaccine") recurrence else "None"
                    )
                    title = ""
                    selectedDate = if (selectedType == "Feeding") "Everyday" else ""
                    selectedTime = ""
                    recurrence = "None"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save to Schedule")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Active Plans & Reminders:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(schedules) { item ->
                val cardColor = when(item.type) {
                    "Visit" -> Color(0xFFE3F2FD)
                    "Vaccine" -> Color(0xFFE8F5E9)
                    else -> Color(0xFFFFF3E0)
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.foodType, style = MaterialTheme.typography.bodyLarge)

                            Text(
                                text = item.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (item.type == "Feeding") "Time: ${item.time} (Daily)" else "Date: ${item.date} | Time: ${item.time}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (item.type == "Vaccine" && item.recurrence != "None") {
                            Text("Repeats: ${item.recurrence}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1B5E20))
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteFeedingTask(context, petId, item.id)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}