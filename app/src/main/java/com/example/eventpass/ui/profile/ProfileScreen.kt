package com.example.eventpass.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.eventpass.ui.theme.DeepSlate
import com.example.eventpass.ui.theme.EventPassTheme
import com.example.eventpass.ui.theme.MidnightPanel
import com.example.eventpass.ui.theme.MutedFog
import com.example.eventpass.ui.theme.NuxtSignal
import com.example.eventpass.ui.theme.SignalWhite
import com.example.eventpass.ui.theme.StatusRed
import com.example.eventpass.ui.theme.SteelOutline
import com.example.eventpass.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val activeEventName = remember {
        sessionManager.getSelectedEventName()?.takeIf { it.isNotBlank() } ?: "Ninguno seleccionado"
    }

    Scaffold(
        containerColor = MidnightPanel,
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil y Seguridad",
                        fontWeight = FontWeight.Bold,
                        color = SignalWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Dashboard",
                            tint = SignalWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightPanel
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = DeepSlate,
                border = BorderStroke(1.dp, SteelOutline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar de operador",
                        modifier = Modifier.size(38.dp),
                        tint = SignalWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Operador EventPass",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SignalWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(color = DeepSlate, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(color = NuxtSignal, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sesión JWT cifrada (AES-256)",
                    color = MutedFog,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Tarjeta 1: Estado de la Terminal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSlate),
                border = BorderStroke(1.dp, SteelOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DIAGNÓSTICO DE TERMINAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedFog,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = SteelOutline)
                    ProfileInfoRow(
                        icon = Icons.Default.Event,
                        label = "Evento activo en puerta",
                        value = activeEventName
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.CloudDone,
                        label = "Servidor Backend (HTTPS)",
                        value = "eventpass-web-cnyn.onrender.com"
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Storage,
                        label = "Motor Offline-First",
                        value = "Room SQLite + Sincronización por Lotes"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 2: Manual de Seguridad, Privacidad y Ética
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSlate),
                border = BorderStroke(1.dp, SteelOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROTOCOLO DE SEGURIDAD Y TRANSPARENCIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedFog,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = SteelOutline)
                    ProfileInfoRow(
                        icon = Icons.Default.Lock,
                        label = "Privacidad y Protección de Datos",
                        value = "Credenciales en EncryptedSharedPreferences y datos mínimos sin uso comercial."
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Shield,
                        label = "Seguridad Anti-Inyección",
                        value = "Sanitización local de códigos QR, tráfico HTTPS obligatorio y cabeceras protegidas."
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.QrCodeScanner,
                        label = "Uso Ético de IA (Google ML Kit)",
                        value = "Reconocimiento óptico 100% local en el dispositivo; sin biometría ni rastreo."
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Eco,
                        label = "Impacto Ambiental (Green Coding)",
                        value = "Polling inteligente de 15s con pausa en segundo plano y boletos 100% digitales (Cero Papel)."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    sessionManager.clearSession()
                    onLogoutSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusRed,
                    contentColor = SignalWhite
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = MidnightPanel,
            border = BorderStroke(1.dp, SteelOutline)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NuxtSignal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MutedFog
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SignalWhite
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    EventPassTheme {
        ProfileScreen(
            onNavigateBack = {},
            onLogoutSuccess = {}
        )
    }
}
