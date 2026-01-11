package com.example.mypetapp.screens

import android.graphics.Bitmap
import java.time.LocalDate

data class Pet(
    var name: String,
    var type: String,
    var breed: String,
    var birthDate: LocalDate,
    var weight: Double,
    var imageBitmap: Bitmap? = null
)