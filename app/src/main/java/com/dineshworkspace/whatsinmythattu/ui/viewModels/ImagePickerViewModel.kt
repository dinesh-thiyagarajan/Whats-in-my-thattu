package com.dineshworkspace.whatsinmythattu.ui.viewModels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(private val contentResolver: ContentResolver) : ViewModel() {

    fun onImageSelected(uri: Uri?) {
        uri?.let {
            val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, it))

            val bitmap = Bitmap.createScaledBitmap(image, 192, 192, true)
            val input = ByteBuffer.allocateDirect(192*192*1*3).order(ByteOrder.nativeOrder())
            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val px = bitmap.getPixel(x, y)

                    // Get channel values from the pixel value.
                    val r = Color.red(px)
                    val g = Color.green(px)
                    val b = Color.blue(px)

                    // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                    // For example, some models might require values to be normalized to the range
                    // [0.0, 1.0] instead.
                    val rf = (r - 127) / 255f
                    val gf = (g - 127) / 255f
                    val bf = (b - 127) / 255f

                    input.putFloat(rf)
                    input.putFloat(gf)
                    input.putFloat(bf)
                }
            }
        }
    }



}