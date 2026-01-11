package com.example.mypetapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mypetapp.screens.*

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val pets = remember { mutableStateListOf<Pet>() }
                val currentUserProfile = remember { mutableStateOf(UserProfile(name = "")) }

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    composable("login") {
                        LoginScreen(
                            onLoginClick = { username: String ->
                                currentUserProfile.value = UserProfile(name = username)
                                navController.navigate("pets") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }


                    composable("pets") {
                        PetProfilesScreen(
                            pets = pets,
                            userProfile = currentUserProfile.value,
                            onAddPetClick = {
                                navController.navigate("addPet")
                            },
                            onPetClick = { index ->
                                navController.navigate("petDetails/$index")
                            }
                        )
                    }

                    composable("addPet") {
                        AddPetScreen(
                            onSave = { newPet: Pet ->
                                pets.add(newPet)
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "petDetails/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        PetDetailsScreen(
                            pet = pets[index],
                            onEdit = { updatedPet: Pet ->
                                pets[index] = updatedPet
                            }
                        )
                    }
                }
            }
        }
    }
}