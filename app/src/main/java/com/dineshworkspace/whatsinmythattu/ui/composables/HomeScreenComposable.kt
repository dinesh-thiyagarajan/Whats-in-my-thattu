package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.dineshworkspace.whatsinmythattu.R

@Composable
fun HomeScreenComposable() {

    var showImagePickerComposable by remember {
        mutableStateOf(false)
    }

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
                    showImagePickerComposable = true
                }
                .size(100.dp),
            painter = painterResource(id = R.drawable.ic_camera),
            contentDescription = "camera",
        )
    }

    if (showImagePickerComposable) {
        ImagePickerComposable {
            showImagePickerComposable = false
        }
    }
}