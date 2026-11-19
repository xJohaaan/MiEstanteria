package com.example.miestanteria.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themePreference: com.example.miestanteria.ui.theme.ThemePreference,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val currentTheme by themePreference.tema.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de perfil
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                OutlinedTextField(
                    value = uiState.nombreUsuario,
                    onValueChange = { settingsViewModel.onNombreUsuarioChange(it) },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = uiState.biografia,
                    onValueChange = { settingsViewModel.onBiografiaChange(it) },
                    label = { Text("Biografía") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Button(
                    onClick = { settingsViewModel.guardarPerfil() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.nombreUsuario.isNotBlank()
                ) {
                    Text("Guardar cambios")
                }
                
                uiState.mensajeExito?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                uiState.mensajeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sección de cambio de contraseña
            Text(
                text = "Seguridad",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = { settingsViewModel.toggleMostrarCambioPassword() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(if (uiState.mostrandoCambioPassword) "Cancelar cambio de contraseña" else "Cambiar contraseña")
            }
            
            if (uiState.mostrandoCambioPassword) {
                OutlinedTextField(
                    value = uiState.passwordActual,
                    onValueChange = { settingsViewModel.onPasswordActualChange(it) },
                    label = { Text("Contraseña actual") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                OutlinedTextField(
                    value = uiState.passwordNueva,
                    onValueChange = { settingsViewModel.onPasswordNuevaChange(it) },
                    label = { Text("Nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                OutlinedTextField(
                    value = uiState.passwordConfirmada,
                    onValueChange = { settingsViewModel.onPasswordConfirmadaChange(it) },
                    label = { Text("Confirmar nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Button(
                    onClick = { settingsViewModel.cambiarPassword() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.passwordActual.isNotBlank() && 
                              uiState.passwordNueva.isNotBlank() && 
                              uiState.passwordConfirmada.isNotBlank()
                ) {
                    Text("Confirmar cambio")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sección de apariencia
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            ThemeOption(
                title = "Seguir sistema",
                description = "Usar el tema del dispositivo",
                selected = currentTheme == AppTheme.SYSTEM,
                onClick = { scope.launch { themePreference.set(AppTheme.SYSTEM) } }
            )
            
            ThemeOption(
                title = "Modo claro",
                description = "Tema claro siempre activo",
                selected = currentTheme == AppTheme.LIGHT,
                onClick = { scope.launch { themePreference.set(AppTheme.LIGHT) } }
            )
            
            ThemeOption(
                title = "Modo oscuro",
                description = "Tema oscuro siempre activo",
                selected = currentTheme == AppTheme.DARK,
                onClick = { scope.launch { themePreference.set(AppTheme.DARK) } }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
