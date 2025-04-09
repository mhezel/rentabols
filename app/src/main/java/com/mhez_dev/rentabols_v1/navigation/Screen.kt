package com.mhez_dev.rentabols_v1.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Items : Screen("items")
    object ItemDetails : Screen("item/{itemId}") {
        fun createRoute(itemId: String) = "item/$itemId"
    }
    object Profile : Screen("profile")
    object Map : Screen("map")
    object AddItem : Screen("add-item")
}
