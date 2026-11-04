package com.example.eventpass.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventpass.data.remote.EventoDto
import com.example.eventpass.domain.model.Assistant
import com.example.eventpass.ui.theme.DeepSlate
import com.example.eventpass.ui.theme.ElevatedSlate
import com.example.eventpass.ui.theme.EventPassTheme
import com.example.eventpass.ui.theme.MidnightPanel
import com.example.eventpass.ui.theme.MutedFog
import com.example.eventpass.ui.theme.NuxtSignal
import com.example.eventpass.ui.theme.NuxtSignalSurface
import com.example.eventpass.ui.theme.SignalWhite
import com.example.eventpass.ui.theme.StatusYellow
import com.example.eventpass.ui.theme.SteelOutline

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToScanner: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh inteligente vinculado al ciclo de vida (Lifecycle.State.RESUMED)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.runLifecycleAwareAutoRefresh()
        }
    }

    DashboardContent(
        uiState = uiState,
        onRefresh = { viewModel.loadDashboardData(silent = false) },
        onSelectEvent = viewModel::selectEvent,
        onRegisterAccess = viewModel::registerAccess,
        onNavigateToScanner = onNavigateToScanner,
        onNavigateToProfile = onNavigateToProfile,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit = {},
    onSelectEvent: (EventoDto) -> Unit = {},
    onRegisterAccess: (Assistant) -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isOffline = uiState.errorMessage != null
    val occupancyFraction = if (uiState.totalCapacity > 0) {
        (uiState.currentAccessCount.toFloat() / uiState.totalCapacity.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(
        containerColor = MidnightPanel,
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EventPass",
                            fontWeight = FontWeight.ExtraBold,
                            color = SignalWhite
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = if (isOffline) StatusYellow.copy(alpha = 0.15f) else NuxtSignalSurface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isOffline) StatusYellow.copy(alpha = 0.4f) else NuxtSignal.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = if (isOffline) "● Offline" else "● En línea",
                                color = if (isOffline) StatusYellow else NuxtSignal,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Recargar ahora",
                            tint = SignalWhite
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Perfil de usuario",
                            tint = SignalWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightPanel
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScanner,
                containerColor = NuxtSignal,
                contentColor = MidnightPanel
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Escanear QR"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Selector robusto de Evento en la cabecera
                EventSelectorHeader(
                    eventos = uiState.eventos,
                    selectedEventName = uiState.selectedEventName,
                    onSelectEvent = onSelectEvent
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = StatusYellow,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MONITOREO DE ACCESO EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedFog,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjetas de métricas más grandes y legibles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Aforo Total",
                        value = "${uiState.currentAccessCount}/${uiState.totalCapacity}",
                        subtitle = "Confirmados / Capacidad",
                        icon = Icons.Filled.Groups,
                        progress = occupancyFraction,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Accesos Hoy",
                        value = uiState.currentAccessCount.toString(),
                        subtitle = "Pases validados con QR",
                        icon = Icons.Filled.Verified,
                        progress = null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onNavigateToScanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NuxtSignal,
                        contentColor = MidnightPanel
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Abrir Escáner QR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ASISTENTES DEL EVENTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedFog,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.recentAssistants.size} registros",
                        style = MaterialTheme.typography.bodySmall,
                        color = NuxtSignal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(uiState.recentAssistants, key = { it.id }) { assistant ->
                        AttendeeRowCard(assistant = assistant)
                    }
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = NuxtSignal,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventSelectorHeader(
    eventos: List<EventoDto>,
    selectedEventName: String,
    onSelectEvent: (EventoDto) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (eventos.isNotEmpty()) {
                expanded = !expanded
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedEventName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Evento Seleccionado") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = "Evento",
                    tint = NuxtSignal
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NuxtSignal,
                unfocusedBorderColor = SteelOutline,
                focusedLabelColor = NuxtSignal,
                unfocusedLabelColor = MutedFog,
                focusedContainerColor = DeepSlate,
                unfocusedContainerColor = DeepSlate,
                focusedTextColor = SignalWhite,
                unfocusedTextColor = SignalWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ElevatedSlate
        ) {
            eventos.forEach { evento ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = evento.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = SignalWhite
                            )
                            Text(
                                text = "Confirmados: ${evento.confirmedCount}/${evento.capacity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NuxtSignal
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelectEvent(evento)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    progress: Float?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 132.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        border = BorderStroke(1.dp, SteelOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedFog
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NuxtSignal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                fontWeight = FontWeight.ExtraBold,
                color = SignalWhite
            )
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = NuxtSignal,
                    trackColor = MidnightPanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedFog
            )
        }
    }
}

@Composable
private fun AttendeeRowCard(
    assistant: Assistant
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        border = BorderStroke(1.dp, SteelOutline.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = NuxtSignalSurface,
                    border = BorderStroke(1.dp, NuxtSignal.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = NuxtSignal
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = assistant.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SignalWhite
                    )
                    Text(
                        text = assistant.folio,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = NuxtSignal
                    )
                }
            }

            Surface(
                color = MidnightPanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SteelOutline)
            ) {
                Text(
                    text = assistant.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedFog,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    EventPassTheme {
        DashboardContent(
            uiState = DashboardUiState(
                selectedEventName = "Conferencia Tech 2026",
                totalCapacity = 500,
                currentAccessCount = 145,
                recentAssistants = listOf(
                    Assistant("3", "Ana Martínez", "EP-7B1A2C3D4E5F6071", "19:42"),
                    Assistant("2", "Luis Herrera", "EP-9F8E7D6C5B4A3210", "19:31"),
                    Assistant("1", "Sofía Rivas", "EP-1A2B3C4D5E6F7081", "19:18")
                )
            ),
            onRegisterAccess = {}
        )
    }
}
