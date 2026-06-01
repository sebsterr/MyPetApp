package com.example.mypetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mypetapp.screens.*
import com.example.mypetapp.viewmodel.AiViewModel
import com.example.mypetapp.viewmodel.PetViewModel
import com.example.mypetapp.viewmodel.FeedingViewModel
import com.example.mypetapp.viewmodel.WeightViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Feeding Schedule"
            val descriptionText = "Notifications for pet feeding times"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH

            val channel = android.app.NotificationChannel("PET_CARE_CHANNEL", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        setContent {
            MaterialTheme {
                val viewModel: PetViewModel = viewModel()
                val feedingViewModel: FeedingViewModel = viewModel()
                val weightViewModel: WeightViewModel = viewModel()
                val aiViewModel: AiViewModel = viewModel()

                val navController = rememberNavController()
                val currentUser by viewModel.currentUser.collectAsState()
                val pets by viewModel.pets.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "pets"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("pets") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }

                    composable("pets") {
                        PetProfilesScreen(
                            pets = pets,
                            userEmail = currentUser?.email ?: "User",
                            onAddPetClick = { navController.navigate("addPet") },
                            onPetClick = { petId ->
                                navController.navigate("petDetails/$petId")
                            },
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate("login") {
                                        popUpTo("pets") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("addPet") {
                        AddPetScreen(existingPet = null) { name, type, breed, date, weight, gender, isNeutered, uri ->
                            viewModel.addPet(name, type, breed, date, weight, gender, isNeutered, uri)
                            navController.popBackStack()
                        }
                    }

                    composable(
                        route = "petDetails/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""

                        val pet = remember(pets, petId) { pets.find { it.id == petId } }

                        if (pet != null) {
                            PetDetailsScreen(
                                pet = pet,
                                navController = navController,
                                onEdit = { selectedPet -> navController.navigate("editPet/${selectedPet.id}") },
                                onDelete = {
                                    viewModel.deletePet(petId)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "aiAssistant/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        val pet = pets.find { it.id == petId }

                        val petName = pet?.name ?: "My Pet"

                        val contextText = if (pet != null) {
                            "The pet's name is ${pet.name}, it is a ${pet.type}, breed ${pet.breed}, gender ${pet.gender}, sterilization status: ${pet.isNeutered}, born on ${pet.birthDate}, and weighs ${pet.weight} kg."
                        } else "Pet info not available."

                        AiAssistantScreen(
                            petName = petName,
                            petContext = contextText,
                            viewModel = aiViewModel
                        )
                    }

                    composable(
                        route = "editPet/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        val pet = pets.find { it.id == petId }

                        AddPetScreen(existingPet = pet) { name, type, breed, date, weight, gender, isNeutered, uri ->
                            if (pet != null) {
                                viewModel.updatePet(
                                    petId = pet.id,
                                    name = name,
                                    type = type,
                                    breed = breed,
                                    date = date,
                                    weight = weight,
                                    gender = gender,
                                    isNeutered = isNeutered,
                                    newUri = uri,
                                    oldImageUrl = pet.imageUrl
                                )
                                navController.popBackStack()
                            }
                        }
                    }

                    composable(
                        route = "feedingSchedule/{petId}/{petName}",
                        arguments = listOf(
                            navArgument("petId") { type = NavType.StringType },
                            navArgument("petName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        val petName = backStackEntry.arguments?.getString("petName") ?: ""

                        FeedingScheduleScreen(
                            petId = petId,
                            petName = petName,
                            viewModel = feedingViewModel
                        )
                    }

                    composable(
                        route = "weightTracker/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""

                        WeightTrackerScreen(
                            petId = petId,
                            viewModel = weightViewModel
                        )
                    }
                }
            }
        }
    }
}