package com.dineshworkspace.whatsinmythattu.ui.composables


import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionComposable() {
    val coroutineScope = rememberCoroutineScope()
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
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
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                when (it.status) {
                    is PermissionStatus.Granted -> {

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