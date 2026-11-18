package com.example.miestanteria.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miestanteria.ui.AppViewModelFactory
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.explore.ExploreScreen
import com.example.miestanteria.ui.explore.ExploreViewModel
import com.example.miestanteria.ui.home.HomeScreen
import com.example.miestanteria.ui.screens.PlaceholderScreen
import com.example.miestanteria.ui.screens.ProfileScreen
import com.example.miestanteria.ui.shelf.ShelfScreen
import com.example.miestanteria.ui.shelf.ShelfViewModel

/** Contenedor de las 5 pestanas principales (barra inferior). */
@Composable
fun MainScreen(
    onLoggedOut: () -> Unit,
    onOpenBook: (String) -> Unit,
    viewModel: MainViewModel = viewModel(factory = AppViewModelFactory.Factory),
    exploreViewModel: ExploreViewModel = viewModel(factory = AppViewModelFactory.Factory),
    shelfViewModel: ShelfViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val biblioteca by viewModel.biblioteca.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(NavTab.INICIO) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val onTabSelected: (NavTab) -> Unit = { currentTab = it }
    val nombre = session?.nombre ?: ""

    // "Atras" en cualquier pestana vuelve primero a Inicio
    BackHandler(enabled = currentTab != NavTab.INICIO) { currentTab = NavTab.INICIO }

    Crossfade(targetState = currentTab, label = "tabs", modifier = Modifier.fillMaxSize()) { tab ->
        when (tab) {
            NavTab.INICIO -> HomeScreen(
                userName = nombre,
                biblioteca = biblioteca,
                onOpenBook = onOpenBook,
                onTabSelected = onTabSelected
            )
            NavTab.EXPLORAR -> ExploreScreen(exploreViewModel, onOpenBook, onTabSelected)
            NavTab.ESTANTERIA -> ShelfScreen(shelfViewModel, onOpenBook, onTabSelected)
            NavTab.COMUNIDAD -> PlaceholderScreen(
                tab, "Aquí verás las recomendaciones y reseñas de otros lectores.", onTabSelected
            )
            NavTab.PERFIL -> ProfileScreen(
                userName = nombre,
                email = session?.email ?: "",
                onLogout = { confirmLogout = true },
                onTabSelected = onTabSelected
            )
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    viewModel.logout(onLoggedOut)
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") }
            }
        )
    }
}
