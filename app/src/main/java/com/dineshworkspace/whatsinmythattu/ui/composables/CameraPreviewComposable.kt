package com.dineshworkspace.whatsinmythattu.ui.composables

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavOptions
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.navigation.NavRouter
import com.dineshworkspace.whatsinmythattu.navigation.Router
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreviewScreen(imageInterpreterViewModel: ImageInterpreterViewModel) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.CAMERA
        ) -> {
            CameraPreviewComposable(imageInterpreterViewModel = imageInterpreterViewModel)
        }

        else -> {
            RequestCameraPermissionComposable(imageInterpreterViewModel)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermissionComposable(imageInterpreterViewModel: ImageInterpreterViewModel) {

    val cameraPermissionState = imageInterpreterViewModel.cameraPermissionState.collectAsState()
    if (cameraPermissionState.value) {
        CameraPreviewComposable(imageInterpreterViewModel = imageInterpreterViewModel)
    }

    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
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
            Manifest.permission.CAMERA -> {
                when (it.status) {
                    is PermissionStatus.Granted -> {
                        SideEffect {
                            imageInterpreterViewModel.updateCameraPermissionState(state = true)
                        }
                    }

                    is PermissionStatus.Denied -> {
                        SideEffect {
                            imageInterpreterViewModel.updateCameraPermissionState(state = false)
                        }
                    }
                }
            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(value = future.get())
        }, executor)
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

@Composable
fun CameraPreviewComposable(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    imageInterpreterViewModel: ImageInterpreterViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val fileDir = LocalContext.current.cacheDir
    val executor = LocalContext.current.executor


    val lifecycleOwner = LocalLifecycleOwner.current
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    this.scaleType = scaleType
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                // CameraX Preview UseCase
                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                coroutineScope.launch {
                    val cameraProvider = context.getCameraProvider()
                    try {
                        // Must unbind the use-cases before rebinding them.
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, previewUseCase, imageCapture
                        )
                    } catch (ex: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", ex)
                    }
                }

                previewView
            }
        )
        Image(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clickable {
                    val photoFile = File(
                        fileDir,
                        "${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions
                        .Builder(photoFile)
                        .build()
                    imageCapture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val uri = Uri.fromFile(photoFile)
                                coroutineScope.launch {
                                    val imageInterpretation = coroutineScope.async {
                                        imageInterpreterViewModel.onImageSelected(uri = uri)
                                    }
                                    imageInterpretation.await()
                                    val navOptions = NavOptions
                                        .Builder()
                                        .setPopUpTo(Router.FoodMatchesRouter.route, true)
                                        .build()
                                    NavRouter.navigate(
                                        route = Router.FoodMatchesRouter.route,
                                        navOptions = navOptions
                                    )
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraCapture", "Image capture failed", exception)
                            }
                        })
                },
            painter = painterResource(id = R.drawable.ic_camera_lens),
            contentDescription = "Camera Capture"
        )
    }

}