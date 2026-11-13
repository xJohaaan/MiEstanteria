package com.example.miestanteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miestanteria.data.repository.SessionState
import com.example.miestanteria.ui.AppViewModelFactory
import com.example.miestanteria.ui.components.AuthHeader
import com.example.miestanteria.ui.splash.SplashViewModel
import com.example.miestanteria.ui.theme.SagePrimary

/** Pantalla de presentacion: muestra el logo y redirige segun haya o no sesion iniciada. */
@Composable
fun SplashScreen(
    onSessionValid: () -> Unit,
    onNoSession: () -> Unit,
    viewModel: SplashViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val destino by viewModel.destino.collectAsStateWithLifecycle()

    LaunchedEffect(destino) {
        when (destino) {
            SessionState.VALIDA -> onSessionValid()
            SessionState.SIN_SESION -> onNoSession()
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader(subtitle = "Tu biblioteca personal", logoSize = 96)
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(color = SagePrimary, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
    }
}
