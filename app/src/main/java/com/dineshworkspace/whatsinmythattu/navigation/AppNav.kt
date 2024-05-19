package com.dineshworkspace.whatsinmythattu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dineshworkspace.whatsinmythattu.ui.composables.CameraPreviewComposable
import com.dineshworkspace.whatsinmythattu.ui.composables.FoodMatchesScreen
import com.dineshworkspace.whatsinmythattu.ui.composables.HomeScreenComposable

private object Route {
    const val HOME_SCREEN = "home"
    const val CAMERA_PREVIEW_SCREEN = "camera_preview"
    const val FOOD_MATCHES_SCREEN = "food_matches/{food_matches}/{on_back_button_pressed}"
}

sealed class Screen(val route: String, val navArguments: List<NamedNavArgument> = emptyList()) {
    object HomeScreen : Screen(route = Route.HOME_SCREEN)
    object CameraPreviewScreen : Screen(route = Route.CAMERA_PREVIEW_SCREEN)
    object FoodMatchesScreen : Screen(route = Route.FOOD_MATCHES_SCREEN)
}


@Composable
fun AppNav(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route
    ) {
        composable(Screen.HomeScreen.route) {
            HomeScreenComposable()
        }
        composable(route = Screen.CameraPreviewScreen.route) {
            CameraPreviewComposable()
        }
        composable(
            route = Screen.FoodMatchesScreen.route
        ) {
            FoodMatchesScreen(
                foodMatches = listOf(),
                onBackButtonPressed = { AppRouter.popBackStack() })
        }

    }
}