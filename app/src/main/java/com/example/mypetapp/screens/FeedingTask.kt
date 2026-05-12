package com.example.mypetapp.screens

data class FeedingTask(
    val id: String = "",
    val petId: String = "",
    val time: String = "08:00",
    val foodType: String = "",
    val quantity: String = "",
    val isEnabled: Boolean = true
)