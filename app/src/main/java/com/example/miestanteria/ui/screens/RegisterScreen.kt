package com.example.miestanteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.miestanteria.ui.auth.RegisterUiState
import com.example.miestanteria.ui.auth.RegisterViewModel
import com.example.miestanteria.ui.auth.Validators
import com.example.miestanteria.ui.components.AuthHeader
import com.example.miestanteria.ui.components.AuthTextField
import com.example.miestanteria.ui.theme.MiEstanteriaTheme
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

@Composable
fun RegisterRoute(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.registered) { if (state.registered) onRegistered() }
    RegisterScreen(
        state = state,
        onNombreChange = viewModel::onNombreChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmacionChange = viewModel::onConfirmacionChange,
        onRegisterClick = viewModel::register,
        onBack = onBack,
        onErrorShown = viewModel::dismissError
    )
}

/** Formulario de registro (RF01) con validacion en tiempo real. */
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, enabled = !state.loading) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(subtitle = "Crea tu cuenta de lector", logoSize = 64)
                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    label = "Nombre de usuario",
                    value = state.nombre,
                    onValueChange = onNombreChange,
                    icon = Icons.Default.Person,
                    placeholder = "Como te verán en la comunidad",
                    error = state.nombreError,
                    enabled = !state.loading,
                    modifier = Modifier.testTag("reg_nombre")
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    label = "Correo electrónico",
                    value = state.email,
                    onValueChange = onEmailChange,
                    icon = Icons.Default.Mail,
                    placeholder = "ejemplo@correo.com",
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                    enabled = !state.loading,
                    modifier = Modifier.testTag("reg_email")
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    label = "Contraseña",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    icon = Icons.Default.Lock,
                    placeholder = "Mínimo ${Validators.MIN_PASSWORD} caracteres",
                    error = state.passwordError,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    enabled = !state.loading,
                    modifier = Modifier.testTag("reg_password")
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    label = "Confirmar contraseña",
                    value = state.confirmacion,
                    onValueChange = onConfirmacionChange,
                    icon = Icons.Default.LockReset,
                    placeholder = "Repite la contraseña",
                    error = state.confirmacionError,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focus.clearFocus()
                        onRegisterClick()
                    },
                    enabled = !state.loading,
                    modifier = Modifier.testTag("reg_confirmacion")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        focus.clearFocus()
                        onRegisterClick()
                    },
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reg_button"),
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
                        Text(text = "Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "¿Ya tienes cuenta? ", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = "Inicia sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary,
                        modifier = Modifier
                            .clickable(enabled = !state.loading) { onBack() }
                            .padding(4.dp)
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
fun RegisterScreenPreview() {
    MiEstanteriaTheme {
        RegisterScreen(
            state = RegisterUiState(nombre = "Sofia", email = "sofia@", emailError = "El correo no es valido"),
            onNombreChange = {}, onEmailChange = {}, onPasswordChange = {}, onConfirmacionChange = {},
            onRegisterClick = {}, onBack = {}
        )
    }
}
