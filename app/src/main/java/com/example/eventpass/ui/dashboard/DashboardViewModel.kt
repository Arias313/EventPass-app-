package com.example.eventpass.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.remote.EventoDto
import com.example.eventpass.data.remote.RetrofitClient
import com.example.eventpass.data.repository.AttendanceRepository
import com.example.eventpass.domain.model.Assistant
import com.example.eventpass.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiService = RetrofitClient.apiService
    private val attendanceRepository = AttendanceRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val isRefreshing = AtomicBoolean(false)
    private var pollingJob: Job? = null

    /**
     * Bucle de auto-refresh inteligente diseñado para ejecutarse dentro de
     * `repeatOnLifecycle(Lifecycle.State.RESUMED)` en el DashboardScreen.
     * - Al entrar o regresar a la pantalla, realiza una carga inmediata.
     * - Luego consulta silenciosamente cada 15 segundos mientras la vista esté en primer plano.
     * - Al abrir el escáner, ir a Perfil o minimizar la app, la corrutina se cancela automáticamente.
     */
    suspend fun runLifecycleAwareAutoRefresh() {
        val isInitialLoad = _uiState.value.eventos.isEmpty() && _uiState.value.recentAssistants.isEmpty()
        performRefresh(silent = !isInitialLoad)

        while (currentCoroutineContext().isActive) {
            delay(AUTO_REFRESH_INTERVAL_MS)
            performRefresh(silent = true)
        }
    }

    /**
     * Inicia manualmente el polling en segundo plano si se requiere fuera de Compose.
     */
    fun startAutoRefresh() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            runLifecycleAwareAutoRefresh()
        }
    }

    /**
     * Pausa el polling inmediatamente para ahorrar batería y datos.
     */
    fun stopAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Carga o recarga manual desde la UI.
     */
    fun loadDashboardData(silent: Boolean = false) {
        viewModelScope.launch {
            performRefresh(silent = silent)
        }
    }

    private suspend fun performRefresh(silent: Boolean) {
        if (!isRefreshing.compareAndSet(false, true)) return

        if (!silent) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        try {
            // 1. Sincronizar cualquier check-in pendiente realizado en modo Offline
            attendanceRepository.syncPendingCheckIns()

            // 2. Consultar eventos actualizados desde Render
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
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    return
                }

                val savedId = sessionManager.getSelectedEventId()
                val activeEvent = eventos.find { it.eventoId == savedId } ?: eventos.first()

                sessionManager.saveSelectedEvent(activeEvent.eventoId, activeEvent.displayName)
                fetchAttendeesWithRepository(activeEvent, eventos)
            } else {
                loadOfflineFallback("Error del servidor (${eventosRes.code()}). Mostrando datos locales.")
            }
        } catch (e: Exception) {
            loadOfflineFallback("Modo Offline activo: mostrando copia local de Room.")
        } finally {
            isRefreshing.set(false)
        }
    }

    /**
     * Permite cambiar el evento activo desde el selector desplegable del Dashboard.
     */
    fun selectEvent(evento: EventoDto) {
        sessionManager.saveSelectedEvent(evento.eventoId, evento.displayName)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedEventId = evento.eventoId,
                    selectedEventName = evento.displayName,
                    isLoading = true,
                    errorMessage = null
                )
            }
            fetchAttendeesWithRepository(evento, _uiState.value.eventos)
        }
    }

    private suspend fun fetchAttendeesWithRepository(
        activeEvent: EventoDto,
        allEvents: List<EventoDto>
    ) {
        val localEntities = attendanceRepository.getAttendeesForEvent(activeEvent.eventoId)
        val checkedInCount = localEntities.count { it.estadoCheckin }
        val checkedInEntities = localEntities.filter { it.estadoCheckin }
        val displayEntities = if (checkedInEntities.isNotEmpty()) checkedInEntities else localEntities

        _uiState.update {
            it.copy(
                eventos = allEvents,
                selectedEventId = activeEvent.eventoId,
                selectedEventName = activeEvent.displayName,
                totalCapacity = activeEvent.capacity,
                currentAccessCount = maxOf(activeEvent.confirmedCount, checkedInCount),
                recentAssistants = displayEntities.map { entity -> entity.toDomainModel() },
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private suspend fun loadOfflineFallback(message: String) {
        val savedId = sessionManager.getSelectedEventId().orEmpty()
        val savedName = sessionManager.getSelectedEventName() ?: "Evento (Modo Offline)"
        val localEntities = if (savedId.isNotBlank()) {
            attendanceRepository.getAttendeesForEvent(savedId)
        } else {
            emptyList()
        }
        val checkedInCount = localEntities.count { it.estadoCheckin }

        _uiState.update {
            it.copy(
                selectedEventId = savedId,
                selectedEventName = savedName,
                currentAccessCount = checkedInCount,
                recentAssistants = localEntities.map { entity -> entity.toDomainModel() },
                isLoading = false,
                errorMessage = message
            )
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

    companion object {
        // Intervalo de 15 segundos para optimizar consumo de batería en el dispositivo móvil
        private const val AUTO_REFRESH_INTERVAL_MS = 15_000L
    }
}
