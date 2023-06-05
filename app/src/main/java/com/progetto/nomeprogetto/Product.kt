package com.progetto.nomeprogetto

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val width: Double,
    val height: Double,
    val length: Double,
    val stock: Int,
    val main_picture: Bitmap?,
    val avgRating: Double,
    val reviewsNumber: Int) : Parcelable