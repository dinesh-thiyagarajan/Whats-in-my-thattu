package com.dineshworkspace.whatsinmythattu.ui.viewModels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dineshworkspace.tensorimageinterpreter.FoodMatch
import com.dineshworkspace.tensorimageinterpreter.TensorImageInterpreter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageInterpreterViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val tensorImageInterpreter: TensorImageInterpreter
) :
    ViewModel() {

    val foodMatches: StateFlow<List<FoodMatch>> get() = _foodMatches
    private val _foodMatches: MutableStateFlow<List<FoodMatch>> = MutableStateFlow(
        listOf()
    )

    val cameraPermissionState: StateFlow<Boolean> get() = _cameraPermissionState
    private val _cameraPermissionState: MutableStateFlow<Boolean> = MutableStateFlow(
        false
    )

    val imagePickerPermissionState: StateFlow<Boolean> get() = _imagePickerPermissionState
    private val _imagePickerPermissionState: MutableStateFlow<Boolean> = MutableStateFlow(
        false
    )

    val launchImagePicker: StateFlow<Boolean> get() = _launchImagePicker
    private val _launchImagePicker: MutableStateFlow<Boolean> = MutableStateFlow(
        false
    )

    val redirectToImagePickerScreen: StateFlow<Boolean> get() = _redirectToImagePickerScreen
    private val _redirectToImagePickerScreen: MutableStateFlow<Boolean> = MutableStateFlow(
        false
    )


    fun onImageSelected(uri: Uri?) {
        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val bitmap = convertUriToBitMap(uri)
                val foodMatches = mutableListOf<FoodMatch>()
                val interpretedMatches: List<FoodMatch> =
                    tensorImageInterpreter.runImageInterpretation(bitmap)
                repeat(interpretedMatches.filter { it.score > 0.0 }.size) { iterator ->
                    if (iterator <= 100) {
                        foodMatches.add(interpretedMatches[iterator])
                    }
                }
                _foodMatches.value = foodMatches
            }
        }
    }

    fun resetProbableFoodMatches() {
        _foodMatches.value = listOf()
    }

    fun updateLaunchImagePicker(state: Boolean) {
        _launchImagePicker.value = state
    }

    fun updateCameraPermissionState(state: Boolean) {
        _cameraPermissionState.value = state
    }

    fun updateImagePickerPermissionState(state: Boolean) {
        _imagePickerPermissionState.value = state
    }

    fun redirectToImagePickerScreen(state: Boolean) {
        _redirectToImagePickerScreen.value = state
    }

    private fun convertUriToBitMap(uri: Uri): Bitmap =
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            .copy(Bitmap.Config.ARGB_8888, true)
}