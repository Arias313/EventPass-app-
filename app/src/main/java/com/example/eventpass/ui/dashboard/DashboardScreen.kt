package com.example.eventpass.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventpass.domain.model.Assistant
import com.example.eventpass.ui.theme.EventPassTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        uiState = uiState,
        onRegisterAccess = viewModel::registerAccess,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRegisterAccess: (Assistant) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("EventPass Admin") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Perfil"
                        )
                    }
                }
            )
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
                Text(
                    text = "Monitoreo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Aforo Total",
                        value = "${uiState.currentAccessCount}/${uiState.totalCapacity}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Accesos Hoy",
                        value = uiState.currentAccessCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val nextCount = uiState.currentAccessCount + 1
                        onRegisterAccess(
                            Assistant(
                                id = nextCount.toString(),
                                name = "Asistente $nextCount",
                                folio = "EP-${1000 + nextCount}",
                                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(Date())
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Abrir Escáner QR")
                }
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Últimos asistentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.recentAssistants, key = { it.id }) { assistant ->
                        ListItem(
                            headlineContent = { Text(assistant.name) },
                            supportingContent = { Text("Folio ${assistant.folio}") },
                            trailingContent = { Text(assistant.timestamp) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    EventPassTheme {
        DashboardContent(
            uiState = DashboardUiState(
                currentAccessCount = 3,
                recentAssistants = listOf(
                    Assistant("3", "Ana Martínez", "EP-1042", "19:42"),
                    Assistant("2", "Luis Herrera", "EP-1038", "19:31"),
                    Assistant("1", "Sofía Rivas", "EP-1031", "19:18")
                )
            ),
            onRegisterAccess = {}
        )
    }
}
