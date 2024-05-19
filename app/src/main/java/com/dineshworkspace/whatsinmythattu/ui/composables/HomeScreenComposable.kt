package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.navigation.AppRouter
import com.dineshworkspace.whatsinmythattu.navigation.Screen
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel


@Composable
fun HomeScreenComposable() {
    val imageInterpreterViewModel: ImageInterpreterViewModel = hiltViewModel()
    val foodMatches = imageInterpreterViewModel.foodMatches.collectAsState()

    var showImagePicker: Boolean by remember {
        mutableStateOf(false)
    }

    if (foodMatches.value.isNotEmpty()) {
        AppRouter.navigate(Screen.FoodMatchesScreen.route)
    } else {
        ConstraintLayout {
            val (cameraImage, divider, folderImage) = createRefs()
            Image(
                modifier = Modifier
                    .constrainAs(cameraImage) {
                        end.linkTo(divider.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(end = 20.dp)
                    .clickable {
                        AppRouter.navigate(Screen.CameraPreviewScreen.route)
                    }
                    .size(50.dp),
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "camera",
            )

            Divider(
                modifier = Modifier
                    .constrainAs(divider) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .width(75.dp)
                    .rotate(90f),
                color = MaterialTheme.colorScheme.outline
            )

            Image(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .constrainAs(folderImage) {
                        start.linkTo(divider.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        showImagePicker = true
                    }
                    .size(50.dp),
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "folder",
            )
        }

    }

    if (showImagePicker) {
        ImagePermissionComposable(onClose = { showImagePicker = false })
    }
}

