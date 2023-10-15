package com.dineshworkspace.tensorimageinterpreter

import com.google.firebase.ml.custom.FirebaseCustomLocalModel

class TensorImageInterpreter {

    init {
        val localModel = FirebaseCustomLocalModel.Builder()
            .setAssetFilePath("whats_in_my_thattu.tflite")
            .build()
    }


}