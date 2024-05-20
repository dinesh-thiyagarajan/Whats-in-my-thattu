package com.dineshworkspace.whatsinmythattu.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController


object NavRouter {
    @SuppressLint("StaticFieldLeak")
    var navController: NavController? = null

    fun navigate(route: String) {
        navController?.navigate(route = route)
    }

    fun popBackStack() {
        navController?.popBackStack()
    }
}
