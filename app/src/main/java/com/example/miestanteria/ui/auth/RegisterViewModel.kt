package com.example.miestanteria.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.AuthGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmacionError: String? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val registered: Boolean = false
)

/**
 * Controlador de la pantalla de registro (RF01).
 * Valida en tiempo real: cada campo muestra su error en cuanto el usuario ya escribio algo.
 */
class RegisterViewModel(private val auth: AuthGateway) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onNombreChange(v: String) = _state.update {
        it.copy(nombre = v, nombreError = if (v.isEmpty()) null else Validators.nombre(v), errorMessage = null)
    }

    fun onEmailChange(v: String) = _state.update {
        it.copy(email = v, emailError = if (v.isEmpty()) null else Validators.email(v), errorMessage = null)
    }

    fun onPasswordChange(v: String) = _state.update {
        it.copy(
            password = v,
            passwordError = if (v.isEmpty()) null else Validators.passwordRegistro(v),
            confirmacionError = if (it.confirmacion.isEmpty()) null else Validators.confirmacion(v, it.confirmacion),
            errorMessage = null
        )
    }

    fun onConfirmacionChange(v: String) = _state.update {
        it.copy(
            confirmacion = v,
            confirmacionError = if (v.isEmpty()) null else Validators.confirmacion(it.password, v),
            errorMessage = null
        )
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    fun register() {
        val s = _state.value
        if (s.loading) return
        val nombreError = Validators.nombre(s.nombre)
        val emailError = Validators.email(s.email)
        val passwordError = Validators.passwordRegistro(s.password)
        val confirmacionError = Validators.confirmacion(s.password, s.confirmacion)
        if (listOf(nombreError, emailError, passwordError, confirmacionError).any { it != null }) {
            _state.update {
                it.copy(
                    nombreError = nombreError, emailError = emailError,
                    passwordError = passwordError, confirmacionError = confirmacionError
                )
            }
            return
        }
        _state.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = auth.register(s.nombre, s.email, s.password)) {
                is NetworkResult.Success -> _state.update { it.copy(loading = false, registered = true) }
                is NetworkResult.Error -> _state.update { it.copy(loading = false, errorMessage = r.message) }
            }
        }
    }
}
