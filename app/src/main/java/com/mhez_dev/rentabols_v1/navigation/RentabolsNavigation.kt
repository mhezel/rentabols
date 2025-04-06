package com.mhez_dev.rentabols_v1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mhez_dev.rentabols_v1.presentation.auth.AuthScreen
import com.mhez_dev.rentabols_v1.presentation.home.HomeScreen
import com.mhez_dev.rentabols_v1.presentation.items.AddItemScreen
import com.mhez_dev.rentabols_v1.presentation.items.ItemDetailsScreen
import com.mhez_dev.rentabols_v1.presentation.map.MapScreen
import com.mhez_dev.rentabols_v1.presentation.profile.ProfileScreen

@Composable
fun RentabolsNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ItemDetails.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            ItemDetailsScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AddItem.route) {
            AddItemScreen(
                onItemAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
