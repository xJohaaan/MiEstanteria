package com.example.miestanteria.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.remote.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val nombreUsuario: String = "",
    val biografia: String = "",
    val isLoading: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val passwordConfirmada: String = "",
    val mostrandoCambioPassword: Boolean = false
)

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mensajeError = null)
            when (val result = authRepository.me()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        nombreUsuario = result.data.nombreUsuario ?: "",
                        biografia = result.data.biografia ?: "",
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mensajeError = result.message
                    )
                }
            }
        }
    }

    fun onNombreUsuarioChange(newValue: String) {
        _uiState.value = _uiState.value.copy(nombreUsuario = newValue, mensajeExito = null)
    }

    fun onBiografiaChange(newValue: String) {
        _uiState.value = _uiState.value.copy(biografia = newValue, mensajeExito = null)
    }

    fun guardarPerfil() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, mensajeError = null, mensajeExito = null)
            when (val result = authRepository.actualizarPerfil(state.nombreUsuario, state.biografia)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mensajeExito = "Perfil actualizado correctamente"
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mensajeError = result.message
                    )
                }
            }
        }
    }

    fun onPasswordActualChange(value: String) {
        _uiState.value = _uiState.value.copy(passwordActual = value)
    }

    fun onPasswordNuevaChange(value: String) {
        _uiState.value = _uiState.value.copy(passwordNueva = value)
    }

    fun onPasswordConfirmadaChange(value: String) {
        _uiState.value = _uiState.value.copy(passwordConfirmada = value)
    }

    fun toggleMostrarCambioPassword() {
        _uiState.value = _uiState.value.copy(
            mostrandoCambioPassword = !_uiState.value.mostrandoCambioPassword,
            passwordActual = "",
            passwordNueva = "",
            passwordConfirmada = "",
            mensajeError = null,
            mensajeExito = null
        )
    }

    fun cambiarPassword() {
        val state = _uiState.value
        
        if (state.passwordNueva.length < 8) {
            _uiState.value = state.copy(mensajeError = "La nueva contraseña debe tener al menos 8 caracteres")
            return
        }
        
        if (state.passwordNueva != state.passwordConfirmada) {
            _uiState.value = state.copy(mensajeError = "Las contraseñas nuevas no coinciden")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, mensajeError = null, mensajeExito = null)
            when (val result = authRepository.cambiarPassword(state.passwordActual, state.passwordNueva)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mostrandoCambioPassword = false,
                        passwordActual = "",
                        passwordNueva = "",
                        passwordConfirmada = "",
                        mensajeExito = "Contraseña cambiada correctamente"
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mensajeError = result.message
                    )
                }
            }
        }
    }
}
