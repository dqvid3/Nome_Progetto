package com.progetto.nomeprogetto.Objects

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
@Parcelize
class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val width: Double,
    val height: Double,
    val length: Double,
    val main_picture: Bitmap?,
    val avgRating: Double,
    val reviewsNumber: Int,
    val uploadDate: LocalDateTime
) : Parcelable