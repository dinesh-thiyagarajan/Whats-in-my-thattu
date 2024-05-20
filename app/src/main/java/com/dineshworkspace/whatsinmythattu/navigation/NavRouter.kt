package com.dineshworkspace.whatsinmythattu.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController
import androidx.navigation.NavOptions


object NavRouter {
    @SuppressLint("StaticFieldLeak")
    var navController: NavController? = null

    fun navigate(route: String, navOptions: NavOptions? = null) {
        navController?.navigate(route = route, navOptions = navOptions)
    }

    fun popBackStack() {
        navController?.popBackStack()
    }
}
