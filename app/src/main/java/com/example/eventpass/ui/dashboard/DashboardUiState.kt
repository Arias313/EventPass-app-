package com.example.eventpass.ui.dashboard

import com.example.eventpass.data.remote.EventoDto
import com.example.eventpass.domain.model.Assistant

data class DashboardUiState(
    val eventos: List<EventoDto> = emptyList(),
    val selectedEventId: String = "",
    val selectedEventName: String = "Cargando evento...",
    val totalCapacity: Int = 0,
    val currentAccessCount: Int = 0,
    val recentAssistants: List<Assistant> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
