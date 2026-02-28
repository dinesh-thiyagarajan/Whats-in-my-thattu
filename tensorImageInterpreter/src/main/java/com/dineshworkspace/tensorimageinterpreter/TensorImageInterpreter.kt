package com.dineshworkspace.tensorimageinterpreter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.dineshworkspace.tensorimageinterpreter.ml.WhatsInMyThattuV2
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TensorImageInterpreter(context: Context) {

    private val model: WhatsInMyThattuV2 = WhatsInMyThattuV2.newInstance(context)

    /** Labels loaded from assets/labels.txt — one label per line, order matches model output. */
    private val labels: List<String> = loadLabels(context)

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

        // Convert TensorImage to TensorBuffer for the v2 model (no metadata)
        val inputBuffer = processedImage.tensorBuffer
        val outputs = model.process(inputBuffer)

        // Get raw probability array from output
        val probabilities = outputs.outputFeature0AsTensorBuffer.floatArray

        // Map probabilities to labels and build FoodMatch list
        return probabilities.mapIndexed { index, score ->
            val label = labels.getOrElse(index) { "unknown_$index" }
            FoodMatch(
                score = score,
                displayName = label,
                label = label.lowercase().replace(" ", "_")
            )
        }
            .filter { it.score > MIN_CONFIDENCE_THRESHOLD }
            .sortedByDescending { it.score }
            .take(MAX_RESULTS)
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
        private const val LABELS_FILE = "labels.txt"

        private fun loadLabels(context: Context): List<String> {
            return try {
                context.assets.open(LABELS_FILE).bufferedReader().readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load labels from $LABELS_FILE", e)
                emptyList()
            }
        }
    }
}
