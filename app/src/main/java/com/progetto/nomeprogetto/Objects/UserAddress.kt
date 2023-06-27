package com.progetto.nomeprogetto.Objects

class UserAddress (
    val name: String,
    val state: String,
    val address_line1: String,
    val address_line2: String = "",
    val cap: String,
    val city: String,
    val county: String,
    val selected: Boolean = false
)