package com.and

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@kotlinx.parcelize.Parcelize
data class DrugDataModel(val category: String = "", val details: MutableList<String> = mutableListOf()): Serializable, Parcelable