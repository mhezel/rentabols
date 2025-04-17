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
import com.mhez_dev.rentabols_v1.presentation.items.EditItemScreen
import com.mhez_dev.rentabols_v1.presentation.items.ItemDetailsScreen
import com.mhez_dev.rentabols_v1.presentation.items.ItemsScreen
import com.mhez_dev.rentabols_v1.presentation.map.MapScreen
import com.mhez_dev.rentabols_v1.presentation.map.FullScreenMapScreen
import com.mhez_dev.rentabols_v1.presentation.onboarding.OnboardingScreen
import com.mhez_dev.rentabols_v1.presentation.profile.EditProfileScreen
import com.mhez_dev.rentabols_v1.presentation.profile.ProfileScreen
import com.mhez_dev.rentabols_v1.presentation.profile.RentTransactionsScreen
import com.mhez_dev.rentabols_v1.presentation.splash.SplashScreen
import com.mhez_dev.rentabols_v1.presentation.profile.UserProfileScreen
import com.mhez_dev.rentabols_v1.presentation.offers.OfferRequestsScreen
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
                    try {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // Handle navigation exception
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    try {
                        // Always navigate to Auth screen after onboarding
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // Handle navigation exception
                    }
                },
                viewModel = koinViewModel()
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToHome = {
                    try {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // Handle navigation exception
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
                currentRoute = currentRoute
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
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToEditItem = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
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
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToFullScreenMap = { itemId ->
                    navController.navigate(Screen.FullScreenMap.createRoute(itemId))
                }
            )
        }
        
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            EditItemScreen(
                itemId = itemId,
                onItemUpdated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToHome = {
                    try {
                        // When signing out, navigate directly to Auth screen 
                        // with clearing back stack to prevent going back to authenticated screens
                        navController.navigate(Screen.Auth.route) {
                            // Clear entire back stack so user can't go back to authenticated screens
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // If primary navigation fails, try simpler approach
                        navController.navigate(Screen.Auth.route)
                    }
                },
                onNavigateToItems = {
                    navController.navigate(Screen.Items.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToOfferRequests = {
                    navController.navigate(Screen.OfferRequests.route)
                },
                onNavigateToRentTransactions = {
                    navController.navigate(Screen.RentTransactions.route)
                },
                currentRoute = currentRoute
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
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

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                }
            )
        }
        
        composable(
            route = Screen.FullScreenMap.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            FullScreenMapScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.OfferRequests.route) {
            OfferRequestsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Profile.route) } },
                onNavigateToItems = { navController.navigate(Screen.Items.route) { popUpTo(Screen.Profile.route) } },
                onNavigateToMap = { navController.navigate(Screen.Map.route) { popUpTo(Screen.Profile.route) } },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) { popUpTo(Screen.Profile.route) { inclusive = true } } },
                currentRoute = currentRoute,
                viewModel = koinViewModel()
            )
        }
        
        composable(Screen.RentTransactions.route) {
            RentTransactionsScreen(
                viewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
