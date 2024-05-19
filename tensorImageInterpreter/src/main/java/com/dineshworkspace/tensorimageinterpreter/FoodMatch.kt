package com.dineshworkspace.tensorimageinterpreter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodMatch(val score: Float, val label: String, val displayName: String) : Parcelable
