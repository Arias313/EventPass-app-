package com.example.eventpass.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.eventpass.domain.model.Assistant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun registerAccess(assistant: Assistant) {
        _uiState.update { state ->
            state.copy(
                currentAccessCount = state.currentAccessCount + 1,
                recentAssistants = listOf(assistant) + state.recentAssistants
            )
        }
    }
}
