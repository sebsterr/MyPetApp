package com.example.mypetapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.time.LocalDate
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PetDetailsScreen(
    pet: Pet,
    onEdit: (Pet) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        AddPetScreen(
            onSave = { updatedPet: Pet ->
                // Update original pet
                pet.name = updatedPet.name
                pet.type = updatedPet.type
                pet.breed = updatedPet.breed
                pet.birthDate = updatedPet.birthDate
                pet.weight = updatedPet.weight
                pet.imageBitmap = updatedPet.imageBitmap

                onEdit(pet)
                isEditing = false
            },
            existingPet = pet
        )
    } else {
        val age = Period.between(pet.birthDate, LocalDate.now())

        Column(

            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text("Pet Details", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (pet.imageBitmap != null) {
                Image(
                    bitmap = pet.imageBitmap!!.asImageBitmap(),
                    contentDescription = "Pet Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow("Name", pet.name)
            DetailRow("Type", pet.type)
            DetailRow("Breed", pet.breed)
            DetailRow("Age", "${age.years} years, ${age.months} months")
            DetailRow("Weight", "${pet.weight} kg")

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { isEditing = true },
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp)
            ) {
                Text("Edit Pet")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(label, fontSize = 14.sp)
    Text(value, fontSize = 18.sp)
}