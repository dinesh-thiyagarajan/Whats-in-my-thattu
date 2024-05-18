package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel

@Composable
fun HomeScreenComposable() {
    val imageInterpreterViewModel: ImageInterpreterViewModel = hiltViewModel()
    val foodMatches = imageInterpreterViewModel.probableFoodMatches.collectAsState()

    var showImagePicker: Boolean by remember {
        mutableStateOf(false)
    }

    var showCameraPreview: Boolean by remember {
        mutableStateOf(false)
    }

    if (foodMatches.value.isNotEmpty()) {
        ProbableFoodMatchesComposable(foodMatches.value) {
            imageInterpreterViewModel.resetProbableFoodMatches()
            showImagePicker = false
        }
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                modifier = Modifier
                    .clickable {
                        showCameraPreview = true
                    }
                    .size(75.dp),
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "camera",
            )

            Divider()

            Image(
                modifier = Modifier
                    .clickable {
                        showImagePicker = true
                    }
                    .size(75.dp),
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "folder",
            )
        }
    }

    if (showImagePicker) {
        ImagePermissionComposable(onClose = { showImagePicker = false })
    }

    if (showCameraPreview) {
        CameraComposable()
    }
}

