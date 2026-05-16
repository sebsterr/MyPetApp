package com.example.mypetapp.screens

data class Pet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val birthDate: String = "",
    val weight: Double = 0.0,
    val gender: String = "Male",
    var isNeutered: String = "No",
    val ownerId: String = "",
    val imageUrl: String = ""
) {
    constructor() : this("", "", "", "", "", 0.0, "Male", "No", "", "")
}