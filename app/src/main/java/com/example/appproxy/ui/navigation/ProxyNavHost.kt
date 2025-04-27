package com.example.appproxy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appproxy.ui.home.HomeDestination
import com.example.appproxy.ui.home.HomeScreen
import com.example.appproxy.ui.item.ItemEditDestination
import com.example.appproxy.ui.item.ItemEditScreen
import com.example.appproxy.ui.item.ItemEntryDestination
import com.example.appproxy.ui.item.ItemEntryScreen
import com.example.appproxy.ui.settings.AppSelectionDestination
import com.example.appproxy.ui.settings.AppSelectionScreen
import com.example.appproxy.ui.settings.ProfileDestination
import com.example.appproxy.ui.settings.ProfileScreen
import com.example.appproxy.ui.settings.SettingsNav

@Composable
fun ProxyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {

        val settingsNav =
            SettingsNav(
                onNavigateToAbout = {},
                onNavigateToAppSelection = { navController.navigate(AppSelectionDestination.route) },
                onNavigateBackHome = {},
            )

        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) },
                navigateToEditItem = {
                    navController.navigate("${ItemEditDestination.route}/${it}")
                },
                navigateToProfile = { navController.navigate(ProfileDestination.route) },
            )
        }
        composable(route = ProfileDestination.route) {
            ProfileScreen(
                settingsNav = settingsNav,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(route = ItemEntryDestination.route) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
//        composable(
//            route = ItemDetailsDestination.routeWithArgs,
//            arguments = listOf(navArgument(ItemDetailsDestination.itemIdArg) {
//                type = NavType.IntType
//            })
//        ) {
//            ItemDetailsScreen(
//                navigateToEditItem = { navController.navigate("${ItemEditDestination.route}/$it") },
//                navigateBack = { navController.navigateUp() }
//            )
//        }

        composable(
            route = ItemEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemEditDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            ItemEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(route = AppSelectionDestination.route) {
            AppSelectionScreen(onNavigateBack = { navController.navigateUp() })
        }
    }
}