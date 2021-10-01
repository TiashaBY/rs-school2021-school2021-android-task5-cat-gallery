package com.rsschool.catsapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cat(
    val id: String,
    val url: String,
    private val width: Int,
    private val height: Int
) : Parcelable
