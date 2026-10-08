package com.brewkery.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.brewkery.app.ui.features.cart.CartViewModel
import com.brewkery.app.ui.features.menu.MenuViewModel
import com.brewkery.app.ui.navigation.BrewkeryNavGraph
import com.brewkery.app.ui.theme.BrewkeryTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )

        val container = (applicationContext as BrewkeryApp).container

        setContent {
            BrewkeryTheme {
                val navController = rememberNavController()
                val menuViewModel: MenuViewModel = viewModel(
                    factory = MenuViewModel.factory(container),
                )
                val cartViewModel: CartViewModel = viewModel(
                    factory = CartViewModel.factory(container),
                )

                BrewkeryNavGraph(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    cartViewModel = cartViewModel,
                )
            }
        }
    }
}
