package com.example.mypetapp.screens

data class FeedingTask(
    val id: String = "",
    val petId: String = "",
    val time: String = "",
    val foodType: String = "",
    val quantity: String = "",
    val type: String = "Feeding",
    val date: String = "",
    val recurrence: String = "None"
) {
    constructor() : this("", "", "", "", "", "Feeding", "", "None")
}