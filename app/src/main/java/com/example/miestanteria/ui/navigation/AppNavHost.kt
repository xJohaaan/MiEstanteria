package com.example.miestanteria.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miestanteria.ui.main.MainScreen
import com.example.miestanteria.ui.screens.LoginRoute
import com.example.miestanteria.ui.screens.RegisterRoute
import com.example.miestanteria.ui.screens.SplashScreen

/** Rutas de navegacion de la app. */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

/** Navega a [route] borrando todo el historial (para que "atras" no regrese al login o al splash). */
private fun NavHostController.goClearing(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onSessionValid = { navController.goClearing(Routes.MAIN) },
                onNoSession = { navController.goClearing(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginRoute(
                onLoggedIn = { navController.goClearing(Routes.MAIN) },
                onCreateAccount = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterRoute(
                onRegistered = { navController.goClearing(Routes.MAIN) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(onLoggedOut = { navController.goClearing(Routes.LOGIN) })
        }
    }
}
