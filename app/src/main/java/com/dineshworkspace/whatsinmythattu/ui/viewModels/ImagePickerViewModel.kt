package com.dineshworkspace.whatsinmythattu.ui.viewModels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dineshworkspace.tensorimageinterpreter.ProbableFoodMatch
import com.dineshworkspace.tensorimageinterpreter.TensorImageInterpreter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val tensorImageInterpreter: TensorImageInterpreter
) :
    ViewModel() {

    val probableFoodTypes: StateFlow<List<ProbableFoodMatch>> get() = _probableFoodTypes
    private val _probableFoodTypes: MutableStateFlow<List<ProbableFoodMatch>> = MutableStateFlow(
        listOf()
    )

    fun onImageSelected(uri: Uri?) {
        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val bitmap = convertUriToBitMap(uri)
                _probableFoodTypes.value = tensorImageInterpreter.runImage(bitmap)
                tensorImageInterpreter.closeModel()
            }
        }
    }

    private fun convertUriToBitMap(uri: Uri): Bitmap =
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            .copy(Bitmap.Config.ARGB_8888, true)
}