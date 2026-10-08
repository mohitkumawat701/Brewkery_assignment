package com.brewkery.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.brewkery.app.ui.features.cart.CartScreen
import com.brewkery.app.ui.features.cart.CartViewModel
import com.brewkery.app.ui.features.detail.ItemDetailScreen
import com.brewkery.app.ui.features.menu.MenuScreen
import com.brewkery.app.ui.features.menu.MenuViewModel
import com.brewkery.app.ui.features.order.OrderStatusScreen

object NavRoutes {
    const val MENU = "menu"
    const val DETAIL = "detail/{itemId}"
    const val CART = "cart"
    const val ORDER = "order"

    fun detail(itemId: Int) = "detail/$itemId"
}

@Composable
fun BrewkeryNavGraph(
    navController: NavHostController,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MENU,
        modifier = modifier,
    ) {
        composable(NavRoutes.MENU) {
            MenuScreen(
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.detail(itemId))
                },
                onCartClick = { navController.navigate(NavRoutes.CART) },
                onOrderClick = { navController.navigate(NavRoutes.ORDER) },
            )
        }

        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            ItemDetailScreen(
                itemId = itemId,
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(NavRoutes.CART) },
            )
        }

        composable(NavRoutes.CART) {
            CartScreen(
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate(NavRoutes.ORDER) {
                        popUpTo(NavRoutes.CART) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.ORDER) {
            OrderStatusScreen(
                cartViewModel = cartViewModel,
                onBackToMenu = {
                    navController.popBackStack()
                },
            )
        }
    }
}
