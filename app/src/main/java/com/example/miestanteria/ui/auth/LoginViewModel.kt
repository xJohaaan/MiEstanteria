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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val loggedIn: Boolean = false
)

/** Controlador de la pantalla de inicio de sesion (RF02). */
class LoginViewModel(private val auth: AuthGateway) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null, errorMessage = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, errorMessage = null) }
    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    fun login() {
        val s = _state.value
        if (s.loading) return
        val emailError = Validators.email(s.email)
        val passwordError = Validators.passwordLogin(s.password)
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        _state.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = auth.login(s.email, s.password)) {
                is NetworkResult.Success -> _state.update { it.copy(loading = false, loggedIn = true) }
                is NetworkResult.Error -> _state.update { it.copy(loading = false, errorMessage = r.message) }
            }
        }
    }
}
