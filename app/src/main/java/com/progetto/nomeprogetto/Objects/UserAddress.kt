package com.progetto.nomeprogetto.Objects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserAddress (
    var id: Int,
    val name: String,
    val state: String,
    val address_line1: String,
    val address_line2: String = "",
    val cap: String,
    val city: String,
    val county: String,
    var selected: Boolean = false
) : Parcelable