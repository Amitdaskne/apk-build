package com.thunder.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Permissions : Screen("permissions")
    object Login : Screen("login")
    object Home : Screen("home")
}

