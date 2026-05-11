package com.example.mypetapp.screens

data class Pet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val birthDate: String = "",
    val weight: Double = 0.0,
    val ownerId: String = "",
    val imageUrl: String = ""
)
