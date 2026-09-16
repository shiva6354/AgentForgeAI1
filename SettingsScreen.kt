package com.agentforge.ai.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentforge.ai.domain.repository.GeminiRepository
import com.agentforge.ai.ui.components.AgentForgeCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: GeminiRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var proxyUrlInput by remember { mutableStateOf(repository.getBaseUrl()) }
    var healthStatus by remember { mutableStateOf<String?>(null) }
    var isCheckingHealth by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("App Settings & Architecture", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        // Backend Proxy Section
        AgentForgeCard {
            Text("AI Backend Proxy Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                "The Android app routes all AI requests through the secure backend proxy. The Gemini API key remains strictly on the server and is never compiled into the APK.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = proxyUrlInput,
                onValueChange = { proxyUrlInput = it },
                label = { Text("Backend URL", fontSize = 11.sp) },
                placeholder = { Text("http://10.0.2.2:3000", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        repository.updateBaseUrl(proxyUrlInput.trim())
                        isCheckingHealth = true
                        healthStatus = null
                        coroutineScope.launch {
                            val res = repository.checkHealth()
                            isCheckingHealth = false
                            healthStatus = if (res.isSuccess) {
                                "Connected successfully to backend proxy!"
                            } else {
                                "Connection error: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isCheckingHealth) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Save & Test Connection", fontSize = 12.sp)
                    }
                }
            }

            healthStatus?.let {
                val isSuccess = it.startsWith("Connected")
                Text(
                    text = it,
                    color = if (isSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Security Notice Card
        AgentForgeCard(
            containerColor = Color(0xFF0F172A)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(Modifier.width(8.dp))
                Text("Zero-Exposure API Key Security", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE2E8F0))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "• Model: gemini-3.8-flash via @google/genai\n• Architecture: Android (Kotlin/Compose) → Express Proxy → Gemini API\n• Sandboxing: Project files isolated to app-private internal storage\n• Network: HTTPS/ClearText emulator loopback support (10.0.2.2)",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Emulator vs Physical Device tips
        AgentForgeCard {
            Text("Testing Tips for Android Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "1. Android Studio Emulator: use http://10.0.2.2:3000\n2. Physical Android Phone (Wi-Fi): use your PC LAN IP, e.g. http://192.168.1.x:3000\n3. Deployed Cloud Run: paste your cloud service URL directly.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}
