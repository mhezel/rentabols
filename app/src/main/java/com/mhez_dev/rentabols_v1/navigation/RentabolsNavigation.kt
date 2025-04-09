package com.mhez_dev.rentabols_v1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mhez_dev.rentabols_v1.presentation.auth.AuthScreen
import com.mhez_dev.rentabols_v1.presentation.home.HomeScreen
import com.mhez_dev.rentabols_v1.presentation.items.AddItemScreen
import com.mhez_dev.rentabols_v1.presentation.items.ItemDetailsScreen
import com.mhez_dev.rentabols_v1.presentation.items.ItemsScreen
import com.mhez_dev.rentabols_v1.presentation.map.MapScreen
import com.mhez_dev.rentabols_v1.presentation.onboarding.OnboardingScreen
import com.mhez_dev.rentabols_v1.presentation.profile.ProfileScreen
import com.mhez_dev.rentabols_v1.presentation.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RentabolsNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = koinViewModel()
            )
        }

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
                onNavigateToItems = {
                    navController.navigate(Screen.Items.route)
                },
                currentRoute = currentRoute,
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Items.route) {
            ItemsScreen(
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                currentRoute = currentRoute
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
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItems = {
                    navController.navigate(Screen.Items.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                currentRoute = currentRoute
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItems = {
                    navController.navigate(Screen.Items.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                currentRoute = currentRoute
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
