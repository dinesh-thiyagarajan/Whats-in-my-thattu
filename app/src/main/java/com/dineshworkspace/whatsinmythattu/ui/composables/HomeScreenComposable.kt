package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImagePickerViewModel

@Composable
fun HomeScreenComposable() {
    val imagePickerViewModel: ImagePickerViewModel = hiltViewModel()
    val foodMatches = imagePickerViewModel.probableFoodMatches.collectAsState()

    var showImagePicker: Boolean by remember {
        mutableStateOf(false)
    }

    if (foodMatches.value.isNotEmpty()) {
        ProbableFoodMatchesComposable(foodMatches.value)
    } else {
        ConstraintLayout {
            val (cameraImg) = createRefs()
            Image(
                modifier = Modifier
                    .constrainAs(cameraImg) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .clickable {
                        showImagePicker = true
                    }
                    .size(100.dp),
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "camera",
            )
        }
    }

    if (showImagePicker) {
        ImagePermissionComposable(onClose = { showImagePicker = false })
    }
}

