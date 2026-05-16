package com.example.mypetapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.mypetapp.screens.WeightEntry
import com.example.mypetapp.viewmodel.WeightViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    petId: String,
    viewModel: WeightViewModel
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.getWeightHistory(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weight Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add weight")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (weightHistory.size > 1) {
                Text(
                    text = "Weight Evolution",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                WeightChart(
                    weightEntries = weightHistory,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add at least 2 entries for the chart",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Entry History",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = weightHistory.reversed()) { entry: WeightEntry ->
                    val formattedDate = remember(entry.date) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(entry.date))
                    }

                    ListItem(
                        headlineContent = { Text("${entry.weight} kg") },
                        supportingContent = { Text(formattedDate) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    if (showDialog) {
        AddWeightDialog(
            onDismiss = { showDialog = false },
            onConfirm = { weight ->
                viewModel.addWeightEntry(petId, weight)
                showDialog = false
            }
        )
    }
}

@Composable
fun WeightChart(weightEntries: List<WeightEntry>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (weightEntries.size < 2) return@Canvas

        val maxWeight = weightEntries.maxOf { it.weight }.coerceAtLeast(1f) * 1.1f
        val minWeight = weightEntries.minOf { it.weight } * 0.9f

        val width = size.width
        val height = size.height
        val spacing = width / (weightEntries.size - 1)

        val points = weightEntries.mapIndexed { index, entry ->
            val x = index * spacing
            val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)
            val y = height - ((entry.weight - minWeight) / weightRange * height)
            Offset(x, y)
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF4CAF50),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        points.forEach { point ->
            drawCircle(
                color = Color(0xFF388E3C),
                radius = 5.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var weightInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Weight") },
        text = {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    weightInput.replace(',', '.').toFloatOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
