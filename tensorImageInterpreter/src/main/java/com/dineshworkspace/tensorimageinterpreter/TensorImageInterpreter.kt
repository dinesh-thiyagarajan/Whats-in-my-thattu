package com.dineshworkspace.tensorimageinterpreter

import android.util.Log
import com.google.firebase.ml.custom.FirebaseCustomLocalModel
import com.google.firebase.ml.custom.FirebaseModelDataType
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions
import com.google.firebase.ml.custom.FirebaseModelInputs
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions
import java.nio.ByteBuffer

class TensorImageInterpreter {

    private var localModel: FirebaseCustomLocalModel = FirebaseCustomLocalModel.Builder()
        .setAssetFilePath("whats_in_my_thattu.tflite")
        .build()

    private val options = FirebaseModelInterpreterOptions.Builder(localModel).build()
    private val interpreter = FirebaseModelInterpreter.getInstance(options)

    private val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
        .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 5))
        .build()

    fun runInterpretation(inputOptions: ByteBuffer) {
        val inputs = FirebaseModelInputs.Builder()
            .add(inputOptions)
            .build()
        interpreter?.run(inputs, inputOutputOptions)
            ?.addOnSuccessListener { result ->
                Log.e("Success", result.toString())
            }
            ?.addOnFailureListener { e ->
                Log.e("Error", e.localizedMessage)
            }
    }


}