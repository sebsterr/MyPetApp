package com.example.mypetapp.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onSave: (Pet) -> Unit,
    existingPet: Pet? = null
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "") }
    var weight by remember { mutableStateOf(existingPet?.weight?.toString() ?: "") }

    val petTypes = listOf("Dog", "Cat", "Hamster", "Parrot")
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(existingPet?.type ?: "") }

    var birthDate by remember { mutableStateOf(existingPet?.birthDate) }
    var imageBitmap by remember { mutableStateOf(existingPet?.imageBitmap) }

    // Image picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            imageBitmap = bitmap
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(if (existingPet == null) "Add Pet" else "Edit Pet ", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Pet image
        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center

        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "Pet Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(android.R.drawable.ic_menu_camera),
                    contentDescription = "Placeholder",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type dropdown
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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

        // Breed
        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Breed") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Birth date picker
        OutlinedTextField(
            value = birthDate?.toString() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Birth Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val today = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            birthDate = LocalDate.of(year, month + 1, day)
                        },
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weight
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = {
                if (
                    name.isNotBlank() &&
                    selectedType.isNotBlank() &&
                    breed.isNotBlank() &&
                    birthDate != null &&
                    weight.toDoubleOrNull() != null
                ) {
                    val pet = Pet(
                        name = name,
                        type = selectedType,
                        breed = breed,
                        birthDate = birthDate!!,
                        weight = weight.toDouble(),
                        imageBitmap = imageBitmap
                    )
                    onSave(pet)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (existingPet == null) "Save" else "Update")
        }
    }
}