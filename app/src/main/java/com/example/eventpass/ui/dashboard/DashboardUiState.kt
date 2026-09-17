package com.example.eventpass.ui.dashboard

import com.example.eventpass.domain.model.Assistant

data class DashboardUiState(
    val totalCapacity: Int = 500,
    val currentAccessCount: Int = 0,
    val recentAssistants: List<Assistant> = emptyList(),
    val isLoading: Boolean = false
)
