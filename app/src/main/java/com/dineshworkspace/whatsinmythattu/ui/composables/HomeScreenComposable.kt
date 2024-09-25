package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.navigation.NavRouter
import com.dineshworkspace.whatsinmythattu.navigation.Router
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel

@Composable
fun HomeScreenComposable(imageInterpreterViewModel: ImageInterpreterViewModel) {
    val showImagePicker = imageInterpreterViewModel.redirectToImagePickerScreen.collectAsState()

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
                    NavRouter.navigate(Router.CameraPreviewRouter.route)
                }
                .size(50.dp),
            painter = painterResource(id = R.drawable.ic_camera),
            contentDescription = "camera"
        )

        HorizontalDivider(
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
                    imageInterpreterViewModel.redirectToImagePickerScreen(true)
                }
                .size(50.dp),
            painter = painterResource(id = R.drawable.ic_folder),
            contentDescription = "folder"
        )
    }

    LaunchedEffect(key1 = showImagePicker.value) {
        if (showImagePicker.value) {
            NavRouter.navigate(Router.ImagePicker.route)
            imageInterpreterViewModel.redirectToImagePickerScreen(false)
        }
    }
}
