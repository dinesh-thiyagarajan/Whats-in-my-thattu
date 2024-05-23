package com.dineshworkspace.whatsinmythattu.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dineshworkspace.whatsinmythattu.ui.composables.CameraPreviewScreen
import com.dineshworkspace.whatsinmythattu.ui.composables.FoodMatchesScreen
import com.dineshworkspace.whatsinmythattu.ui.composables.HomeScreenComposable
import com.dineshworkspace.whatsinmythattu.ui.composables.ImagePickerScreen
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel

private object Route {
    const val HOME_SCREEN = "home"
    const val CAMERA_PREVIEW_SCREEN = "camera_preview"
    const val FOOD_MATCHES_SCREEN = "food_matches"
    const val IMAGE_PICKER = "image_picker"
}

sealed class Router(val route: String, val navArguments: List<NamedNavArgument> = emptyList()) {
    data object HomeRouter : Router(route = Route.HOME_SCREEN)
    data object CameraPreviewRouter : Router(route = Route.CAMERA_PREVIEW_SCREEN)
    data object FoodMatchesRouter : Router(route = Route.FOOD_MATCHES_SCREEN)
    data object ImagePicker : Router(route = Route.IMAGE_PICKER)
}


@Composable
fun NavGraph(navController: NavHostController) {
    val imageInterpreterViewModel: ImageInterpreterViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = Router.HomeRouter.route
    ) {
        composable(Router.HomeRouter.route) {
            HomeScreenComposable(imageInterpreterViewModel = imageInterpreterViewModel)
        }
        composable(route = Router.CameraPreviewRouter.route) {
            CameraPreviewScreen(imageInterpreterViewModel = imageInterpreterViewModel)
        }
        composable(route = Router.ImagePicker.route) {
            ImagePickerScreen(
                imageInterpreterViewModel = imageInterpreterViewModel,
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Router.FoodMatchesRouter.route
        ) {
            FoodMatchesScreen(
                imageInterpreterViewModel = imageInterpreterViewModel,
                onBackButtonPressed = {
                    navController.popBackStack(
                        route = Router.HomeRouter.route,
                        inclusive = false
                    )
                })
        }

    }
}