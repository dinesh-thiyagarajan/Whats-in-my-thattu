package com.dineshworkspace.whatsinmythattu.ui.composables


import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImagePickerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePickerComposable(onClose: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val imagePickerViewModel: ImagePickerViewModel = hiltViewModel()
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    )

    BackHandler {
        onClose()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissionStates.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                onClose()
                return@rememberLauncherForActivityResult
            }
            imagePickerViewModel.onImageSelected(uri = uri)
        }

    permissionStates.permissions.forEach {
        when (it.permission) {
            Manifest.permission.READ_MEDIA_IMAGES -> {
                when (it.status) {
                    is PermissionStatus.Granted -> {
                        SideEffect {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }

                    is PermissionStatus.Denied -> {

                    }
                }
            }

            Manifest.permission.CAMERA -> {
                when (it.status) {
                    is PermissionStatus.Granted -> {
                        SideEffect {
                            coroutineScope.launch {
                                /*TODO("Implement camera preview and capture option")*/
                            }
                        }
                    }

                    is PermissionStatus.Denied -> {

                    }
                }
            }
        }
    }
}
