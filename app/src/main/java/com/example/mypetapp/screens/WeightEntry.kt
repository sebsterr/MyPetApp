package com.example.mypetapp.screens

data class WeightEntry(
    val id: String = "",
    val weight: Float = 0f,
    val date: Long = System.currentTimeMillis()
)