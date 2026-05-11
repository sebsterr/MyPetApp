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
import com.example.mypetapp.viewmodel.PetViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val viewModel: PetViewModel = viewModel()
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
                        AddPetScreen(existingPet = null) { name, type, breed, date, weight, uri ->
                            viewModel.addPet(name, type, breed, date, weight, uri)
                            navController.popBackStack()
                        }
                    }
                    composable(
                        route = "petDetails/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        val pet = pets.find { it.id == petId }

                        if (pet != null) {
                            PetDetailsScreen(
                                pet = pet,
                                onEdit = { navController.navigate("editPet/${pet.id}") },
                                onDelete = {
                                    viewModel.deletePet(petId)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "editPet/{petId}",
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        val pet = pets.find { it.id == petId }

                        AddPetScreen(existingPet = pet) { name, type, breed, date, weight, uri ->
                            if (pet != null) {
                                viewModel.updatePet(pet.id, name, type, breed, date, weight, uri, pet.imageUrl)
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
