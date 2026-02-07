package com.dineshworkspace.tensorimageinterpreter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodMatch(val score: Float, val label: String, val displayName: String) : Parcelable {
    val imageRandomId get() = (1..8).random()

    /** Confidence score formatted as a percentage string, e.g. "87.3%" */
    val confidencePercent: String
        get() = "%.1f%%".format(score * 100)

    /** True if this match has reasonably high confidence */
    val isHighConfidence: Boolean
        get() = score >= HIGH_CONFIDENCE_THRESHOLD

    companion object {
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.10f
    }
}
