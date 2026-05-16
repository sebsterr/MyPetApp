package com.example.mypetapp.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    existingPet: Pet? = null,
    onSave: (String, String, String, String, Double, String, String, Uri?) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "") }
    var weight by remember { mutableStateOf(existingPet?.weight?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(existingPet?.type ?: "Dog") }
    var birthDate by remember { mutableStateOf(existingPet?.birthDate ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var gender by remember { mutableStateOf(existingPet?.gender ?: "Male") }
    var isNeutered by remember { mutableStateOf(existingPet?.isNeutered ?: "No") }
    val petTypes = listOf("Dog", "Cat", "Hamster", "Parrot")
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (existingPet == null) "Add a Pet" else "Update Pet",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Pet Image",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(android.R.drawable.ic_menu_camera),
                        contentDescription = "Choose image",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    petTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            birthDate = "$y-${m + 1}-$d"
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
            ) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Birth Date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Gender", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (gender == "Male"),
                    onClick = { gender = "Male" }
                )
                Text(text = "Male", modifier = Modifier.clickable { gender = "Male" })

                Spacer(modifier = Modifier.width(24.dp))

                RadioButton(
                    selected = (gender == "Female"),
                    onClick = { gender = "Female" }
                )
                Text(text = "Female", modifier = Modifier.clickable { gender = "Female" })
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Sterilized / Neutered Status", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (isNeutered == "Yes"),
                    onClick = { isNeutered = "Yes" }
                )
                Text(text = "Yes ", modifier = Modifier.clickable { isNeutered = "Yes" })

                Spacer(modifier = Modifier.width(24.dp))

                RadioButton(
                    selected = (isNeutered == "No"),
                    onClick = { isNeutered = "No" }
                )
                Text(text = "No ", modifier = Modifier.clickable { isNeutered = "No" })
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val parsedWeight = weight.toDoubleOrNull()

                    if (name.isBlank() || breed.isBlank() || birthDate.isBlank() || weight.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                    } else if (parsedWeight == null || parsedWeight <= 0.0) {
                        Toast.makeText(context, "Please enter a valid weight greater than 0!", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(name, selectedType, breed, birthDate, parsedWeight, gender, isNeutered, imageUri)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save to Cloud")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}