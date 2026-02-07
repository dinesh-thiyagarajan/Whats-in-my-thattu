package com.dineshworkspace.tensorimageinterpreter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.dineshworkspace.tensorimageinterpreter.ml.WhatsInMyThattu
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class TensorImageInterpreter(context: Context) {

    private val model: WhatsInMyThattu = WhatsInMyThattu.newInstance(context)

    /**
     * Image processor that handles resizing and normalization to match
     * the training pipeline expectations:
     * - Resize to 224x224 using bilinear interpolation
     * - Normalize pixel values from [0, 255] to [0, 1]
     */
    private val imageProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD))
        .build()

    fun runImageInterpretation(bitmap: Bitmap): List<FoodMatch> {
        val processedImage = preprocessImage(bitmap)
        val outputs = model.process(processedImage)

        return outputs.probabilityAsCategoryList
            .filter { it.score > MIN_CONFIDENCE_THRESHOLD }
            .sortedByDescending { it.score }
            .take(MAX_RESULTS)
            .map {
                FoodMatch(
                    score = it.score,
                    displayName = it.displayName.ifEmpty { formatLabel(it.label) },
                    label = it.label
                )
            }
    }

    /**
     * Preprocess bitmap to match the model's expected input format.
     * Ensures consistent image processing regardless of source (camera vs gallery).
     */
    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val argbBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val tensorImage = TensorImage.fromBitmap(argbBitmap)
        return imageProcessor.process(tensorImage)
    }

    /**
     * Convert snake_case or raw labels into human-readable display names.
     * e.g., "chicken_curry" -> "Chicken Curry"
     */
    private fun formatLabel(label: String): String {
        return label
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }

    fun closeModel() {
        try {
            model.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing model", e)
        }
    }

    companion object {
        private const val TAG = "TensorImageInterpreter"
        private const val INPUT_SIZE = 224
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 255f
        private const val MIN_CONFIDENCE_THRESHOLD = 0.01f
        private const val MAX_RESULTS = 50
    }
}
