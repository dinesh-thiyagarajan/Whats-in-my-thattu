package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImagePickerViewModel

@Composable
fun HomeScreenComposable(paddingValues: PaddingValues) {
    val imagePickerViewModel: ImagePickerViewModel = hiltViewModel()
    val foodMatches = imagePickerViewModel.probableFoodMatches.collectAsState()

    var showImagePicker: Boolean by remember {
        mutableStateOf(false)
    }

    if (foodMatches.value.isNotEmpty()) {
        ProbableFoodMatchesComposable(foodMatches.value, paddingValues = paddingValues) {
            imagePickerViewModel.resetProbableFoodMatches()
            showImagePicker = false
        }
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                modifier = Modifier
                    .clickable {
                        showImagePicker = true
                    }
                    .size(50.dp),
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "camera",
            )
        }
    }

    if (showImagePicker) {
        ImagePermissionComposable(onClose = { showImagePicker = false })
    }
}

