package com.progetto.nomeprogetto.Objects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserCard (
    var id: Int,
    val name: String,
    val card_number: String,
    val expiration_date: String,
    val cvv: Int,
    var selected: Boolean = false
) : Parcelable