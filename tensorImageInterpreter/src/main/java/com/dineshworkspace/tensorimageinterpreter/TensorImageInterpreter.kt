package com.dineshworkspace.tensorimageinterpreter


import android.content.Context
import android.graphics.Bitmap
import com.dineshworkspace.tensorimageinterpreter.ml.WhatsInMyThattu
import org.tensorflow.lite.support.image.TensorImage

class TensorImageInterpreter(context: Context) {

    private val model: WhatsInMyThattu = WhatsInMyThattu.newInstance(context)

    fun runImage(bitmap: Bitmap): List<ProbableFoodMatch> {
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        return outputs.probabilityAsCategoryList.sortedByDescending { it.score }.map {
            ProbableFoodMatch(
                score = it.score,
                displayName = it.displayName,
                label = it.label
            )
        }.toList()
    }

    fun closeModel() {
        model.close()
    }

}