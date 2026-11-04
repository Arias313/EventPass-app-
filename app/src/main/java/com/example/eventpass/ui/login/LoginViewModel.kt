package com.example.eventpass.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.remote.LoginRequest
import com.example.eventpass.data.remote.RetrofitClient
import com.example.eventpass.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data class Error(val message: String) : LoginState
}

data class LoginUiState(
    val userOrEmail: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val loginState: LoginState = LoginState.Idle
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUserOrEmailChanged(value: String) {
        _uiState.update { it.copy(userOrEmail = value, loginState = LoginState.Idle) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, loginState = LoginState.Idle) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        val userOrEmail = currentState.userOrEmail.trim()
        val password = currentState.password.trim()

        if (userOrEmail.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(loginState = LoginState.Error("Por favor, ingresa tus credenciales."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loginState = LoginState.Loading) }

            try {
                val response = apiService.login(
                    LoginRequest(
                        userOrEmail = userOrEmail,
                        password = password
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.jwtToken
                    if (token.isNotBlank()) {
                        // Guardar token JWT de forma segura en EncryptedSharedPreferences
                        sessionManager.saveAuthToken(token)

                        // Precargar el primer evento activo de la cuenta para el check-in
                        runCatching {
                            val eventosRes = apiService.getEventos()
                            val firstEvent = eventosRes.body()?.eventos?.firstOrNull()
                            if (firstEvent != null && firstEvent.eventoId.isNotBlank()) {
                                sessionManager.saveSelectedEvent(
                                    eventoId = firstEvent.eventoId,
                                    eventoNombre = firstEvent.displayName
                                )
                            }
                        }

                        _uiState.update { it.copy(loginState = LoginState.Success) }
                    } else {
                        _uiState.update {
                            it.copy(loginState = LoginState.Error("Respuesta inválida: token no encontrado en backend."))
                        }
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Credenciales inválidas. Verifica usuario y contraseña."
                        404 -> "Usuario no registrado."
                        500 -> "Error en el servidor backend."
                        else -> "Error de autenticación (${response.code()}): ${response.message()}"
                    }
                    _uiState.update { it.copy(loginState = LoginState.Error(errorMsg)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loginState = LoginState.Error(
                            "No se pudo conectar al servidor en Render. Verifica tu conexión a internet o espera unos segundos si el servidor estaba en reposo."
                        )
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { it.copy(loginState = LoginState.Idle) }
    }
}
