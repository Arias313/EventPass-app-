package com.example.eventpass.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScannerState {
    object Idle : ScannerState() // Esperando código
    object Processing : ScannerState() // Simulando carga
    data class Valid(val message: String) : ScannerState() // Boleto correcto
    data class Invalid(val message: String) : ScannerState() // Boleto incorrecto
}

class ScannerViewModel : ViewModel() {
    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    fun validateTicket(qrCode: String) {
        // Si ya está procesando un código, ignora los demás (reemplaza a tu lastScannedCode)
        if (_scannerState.value != ScannerState.Idle) return

        viewModelScope.launch {
            _scannerState.value = ScannerState.Processing
            
            // Simulamos que va a internet a revisar el boleto (1 segundo)
            delay(1000)

            // Lógica de prueba: Válido si tiene más de 5 caracteres
            if (qrCode.length > 5) {
                _scannerState.value = ScannerState.Valid("Acceso Permitido\n$qrCode")
            } else {
                _scannerState.value = ScannerState.Invalid("Boleto Inválido\n$qrCode")
            }
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}