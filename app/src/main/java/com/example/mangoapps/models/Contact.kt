package com.example.mangoapps.models

import android.graphics.Bitmap

data class Contact(
    val id: String,
    val name: String,
    var number: List<String>?,
    var image: List<Bitmap>?
)
