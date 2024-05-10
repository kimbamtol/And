package com.and.datamodel

import android.os.Parcelable
import java.io.Serializable

@kotlinx.parcelize.Parcelize
data class DrugDataModel(
    val category: String = "",
    val details: MutableList<String> = mutableListOf(),
    val creationTime: Long = 0L
) : Serializable, Parcelable

data class UserDataModel(
    var name: String = "",
    var birth: String = "",
    var myEmail: String = ""
)