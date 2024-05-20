package com.dineshworkspace.whatsinmythattu.ui.composables


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dineshworkspace.whatsinmythattu.navigation.NavRouter
import com.dineshworkspace.whatsinmythattu.navigation.Router
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun ImagePickerScreen(
    imageInterpreterViewModel: ImageInterpreterViewModel,
    onClose: () -> Unit
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.READ_MEDIA_IMAGES
        ) -> {
            ImagePickerComposable(
                imageInterpreterViewModel = imageInterpreterViewModel,
                onClose = onClose
            )
        }

        else -> {
            RequestImagePickerPermissionComposable(
                imageInterpreterViewModel = imageInterpreterViewModel,
                onClose = onClose
            )
        }
    }

    BackHandler {
        onClose()
    }
}

@Composable
internal fun ImagePickerComposable(
    imageInterpreterViewModel: ImageInterpreterViewModel,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                onClose()
                return@rememberLauncherForActivityResult
            }
            coroutineScope.launch {
                val imageInterpretation = coroutineScope.async {
                    imageInterpreterViewModel.onImageSelected(uri = uri)
                }
                imageInterpretation.await()
                onClose.invoke()
                NavRouter.navigate(Router.FoodMatchesRouter.route, )
            }
        }
    LaunchedEffect(key1 = Unit) {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RequestImagePickerPermissionComposable(
    imageInterpreterViewModel: ImageInterpreterViewModel,
    onClose: () -> Unit
) {
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )
    )

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

    permissionStates.permissions.forEach {
        when (it.permission) {
            Manifest.permission.READ_MEDIA_IMAGES -> {
                when (it.status) {
                    is PermissionStatus.Granted -> {
                        ImagePickerComposable(
                            imageInterpreterViewModel = imageInterpreterViewModel,
                            onClose = onClose
                        )
                    }

                    is PermissionStatus.Denied -> {
                        SideEffect {
                            imageInterpreterViewModel.updateImagePickerPermissionState(false)
                        }
                    }
                }
            }
        }
    }
}
