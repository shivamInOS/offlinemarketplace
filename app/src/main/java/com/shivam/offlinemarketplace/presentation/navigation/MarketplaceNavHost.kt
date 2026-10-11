package com.shivam.offlinemarketplace.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shivam.offlinemarketplace.presentation.edit.EditListingScreen
import com.shivam.offlinemarketplace.presentation.listings.MarketplaceListingsScreen

@Composable
fun MarketplaceNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LISTINGS,
        modifier = modifier
    ) {
        composable(NavRoutes.LISTINGS) {
            MarketplaceListingsScreen(
                onNavigateToCreate = {
                    navController.navigate(NavRoutes.CREATE)
                },
                onNavigateToEdit = { listingId ->
                    navController.navigate(NavRoutes.editRoute(listingId))
                }
            )
        }

        composable(
            route = NavRoutes.EDIT,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId")
            EditListingScreen(
                listingId = listingId,
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }
    }
}
