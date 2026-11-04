package com.example.eventpass.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.remote.RetrofitClient
import com.example.eventpass.data.repository.AttendanceRepository
import com.example.eventpass.data.repository.CheckInOutcome
import com.example.eventpass.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de UI para la pantalla del escáner QR.
 */
sealed interface ScannerState {
    data object Idle : ScannerState // Listo para leer
    data object Processing : ScannerState // Procesando lectura (Render o Room Offline)
    data class Valid(val message: String, val code: String) : ScannerState // Acceso autorizado
    data class Invalid(val message: String, val code: String) : ScannerState // Acceso denegado o error
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiService = RetrofitClient.apiService
    private val attendanceRepository = AttendanceRepository(application)

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    /**
     * Obtiene el ID del evento activo desde SessionManager o consulta el primer evento del usuario en Render.
     */
    private suspend fun resolveActiveEventoId(): String? {
        val savedId = sessionManager.getSelectedEventId()
        if (!savedId.isNullOrBlank()) return savedId

        return runCatching {
            val res = apiService.getEventos()
            val firstEvent = res.body()?.eventos?.firstOrNull()
            if (firstEvent != null && firstEvent.eventoId.isNotBlank()) {
                sessionManager.saveSelectedEvent(firstEvent.eventoId, firstEvent.displayName)
                firstEvent.eventoId
            } else {
                null
            }
        }.getOrNull()
    }

    /**
     * Procesa el código QR escaneado delegando en AttendanceRepository (Online en Render / Offline en Room).
     */
    fun onQrScanned(qrCode: String) {
        if (_scannerState.value !is ScannerState.Idle) return

        val cleanCode = qrCode.trim()
        if (cleanCode.isEmpty()) return

        viewModelScope.launch {
            _scannerState.value = ScannerState.Processing

            val eventoId = resolveActiveEventoId()
            if (eventoId.isNullOrBlank()) {
                _scannerState.value = ScannerState.Invalid(
                    message = "No tienes eventos seleccionados o creados",
                    code = cleanCode
                )
                delay(2500)
                _scannerState.value = ScannerState.Idle
                return@launch
            }

            when (val outcome = attendanceRepository.validateAndRegisterCheckIn(eventoId, cleanCode)) {
                is CheckInOutcome.Valid -> {
                    _scannerState.value = ScannerState.Valid(
                        message = outcome.message,
                        code = outcome.code
                    )
                }

                is CheckInOutcome.Invalid -> {
                    _scannerState.value = ScannerState.Invalid(
                        message = outcome.message,
                        code = outcome.code
                    )
                }
            }

            // Muestra el resultado estructural durante 2.5 segundos
            delay(2500)

            // Restaura el escáner a "listo para leer"
            _scannerState.value = ScannerState.Idle
        }
    }

    /**
     * Alias de compatibilidad para procesar el boleto.
     */
    fun validateTicket(qrCode: String) = onQrScanned(qrCode)

    /**
     * Restablece el escáner a Idle de forma manual.
     */
    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}
