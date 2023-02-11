package com.dineshworkspace.whatsinmythattu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dineshworkspace.whatsinmythattu.camera.CameraScreen
import com.dineshworkspace.whatsinmythattu.ui.theme.WhatsInMyThattuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsInMyThattuTheme {
                CameraScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WhatsInMyThattuTheme {
        CameraScreen()
    }
}