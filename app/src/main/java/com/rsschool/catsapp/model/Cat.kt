package com.rsschool.catsapp.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

/**
 * val json = getJson() // your json value here
val topic = Gson().fromJson(json, Json4Kotlin_Base::class.java)
 */
@Parcelize
data class Cat(val id: String,
               val url : String,
               private val width : Int,
               private val height : Int,
) : Parcelable
