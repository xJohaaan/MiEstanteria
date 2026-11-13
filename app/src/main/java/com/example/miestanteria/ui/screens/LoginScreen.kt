package com.example.miestanteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miestanteria.ui.AppViewModelFactory
import com.example.miestanteria.ui.auth.LoginUiState
import com.example.miestanteria.ui.auth.LoginViewModel
import com.example.miestanteria.ui.components.AuthHeader
import com.example.miestanteria.ui.components.AuthTextField
import com.example.miestanteria.ui.theme.MiEstanteriaTheme
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** Pantalla de inicio de sesion conectada a su ViewModel. */
@Composable
fun LoginRoute(
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loggedIn) { if (state.loggedIn) onLoggedIn() }
    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onCreateAccountClick = onCreateAccount,
        onErrorShown = viewModel::dismissError
    )
}

/** Vista (sin logica): solo dibuja el estado que recibe. */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onErrorShown: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            onErrorShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                AuthHeader(subtitle = "Tu biblioteca personal")
                Spacer(modifier = Modifier.height(36.dp))

                AuthTextField(
                    label = "Correo electrónico",
                    value = state.email,
                    onValueChange = onEmailChange,
                    icon = Icons.Default.Mail,
                    placeholder = "ejemplo@correo.com",
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                    enabled = !state.loading,
                    modifier = Modifier.testTag("login_email")
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    label = "Contraseña",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    icon = Icons.Default.Lock,
                    placeholder = "••••••••",
                    error = state.passwordError,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focus.clearFocus()
                        onLoginClick()
                    },
                    enabled = !state.loading,
                    modifier = Modifier.testTag("login_password")
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        focus.clearFocus()
                        onLoginClick()
                    },
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SagePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = SagePrimary.copy(alpha = 0.7f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text(text = "Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "¿Aún no tienes cuenta? ", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = "Crear cuenta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary,
                        modifier = Modifier
                            .clickable(enabled = !state.loading) { onCreateAccountClick() }
                            .padding(4.dp)
                            .testTag("go_register")
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MiEstanteriaTheme {
        LoginScreen(
            state = LoginUiState(email = "sofia@correo.com"),
            onEmailChange = {}, onPasswordChange = {}, onLoginClick = {}, onCreateAccountClick = {}
        )
    }
}
