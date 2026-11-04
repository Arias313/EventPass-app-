package com.example.eventpass.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.remote.EventoDto
import com.example.eventpass.data.remote.RetrofitClient
import com.example.eventpass.domain.model.Assistant
import com.example.eventpass.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Consulta los eventos reales desde Render y los asistentes del evento activo.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val eventosRes = apiService.getEventos()
                if (eventosRes.isSuccessful) {
                    val eventos = eventosRes.body()?.eventos.orEmpty()
                    if (eventos.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                eventos = emptyList(),
                                selectedEventId = "",
                                selectedEventName = "Sin eventos creados en la web",
                                totalCapacity = 0,
                                currentAccessCount = 0,
                                recentAssistants = emptyList(),
                                isLoading = false
                            )
                        }
                        return@launch
                    }

                    val savedId = sessionManager.getSelectedEventId()
                    val activeEvent = eventos.find { it.eventoId == savedId } ?: eventos.first()

                    sessionManager.saveSelectedEvent(activeEvent.eventoId, activeEvent.displayName)
                    fetchAttendeesForEvent(activeEvent, eventos)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar eventos (${eventosRes.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sin conexión con el servidor en Render"
                    )
                }
            }
        }
    }

    /**
     * Permite cambiar el evento activo para el monitoreo y el escáner QR.
     */
    fun selectEvent(evento: EventoDto) {
        sessionManager.saveSelectedEvent(evento.eventoId, evento.displayName)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchAttendeesForEvent(evento, _uiState.value.eventos)
        }
    }

    private suspend fun fetchAttendeesForEvent(
        activeEvent: EventoDto,
        allEvents: List<EventoDto>
    ) {
        val assistantsList = runCatching {
            val res = apiService.getAsistentes(activeEvent.eventoId)
            val rawList = res.body()?.asistentes.orEmpty()
            // Priorizar asistentes con check-in confirmado, o mostrar los registrados recientes
            val checkedIn = rawList.filter { it.isCheckedIn }
            val source = if (checkedIn.isNotEmpty()) checkedIn else rawList
            source.map { dto ->
                Assistant(
                    id = dto.asistenteId.ifBlank { dto.hashCode().toString() },
                    name = dto.nombre?.takeIf { it.isNotBlank() } ?: "Asistente",
                    folio = dto.pin?.takeIf { it.isNotBlank() } ?: "Sin PIN",
                    timestamp = if (dto.isCheckedIn) {
                        formatIsoTime(dto.fechaCheckin) ?: "Confirmado"
                    } else {
                        "Registrado"
                    }
                )
            }
        }.getOrDefault(emptyList())

        _uiState.update {
            it.copy(
                eventos = allEvents,
                selectedEventId = activeEvent.eventoId,
                selectedEventName = activeEvent.displayName,
                totalCapacity = activeEvent.capacity,
                currentAccessCount = activeEvent.confirmedCount,
                recentAssistants = assistantsList,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun formatIsoTime(isoString: String?): String? {
        if (isoString.isNullOrBlank()) return null
        // Extraer HH:mm de una cadena ISO 8601 (ej. 2026-11-04T17:30:00.000Z)
        val tIndex = isoString.indexOf('T')
        return if (tIndex != -1 && isoString.length >= tIndex + 6) {
            isoString.substring(tIndex + 1, tIndex + 6)
        } else {
            null
        }
    }

    fun registerAccess(assistant: Assistant) {
        _uiState.update { state ->
            state.copy(
                currentAccessCount = state.currentAccessCount + 1,
                recentAssistants = listOf(assistant) + state.recentAssistants
            )
        }
    }
}
